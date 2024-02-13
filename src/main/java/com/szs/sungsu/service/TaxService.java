package com.szs.sungsu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szs.sungsu.api.response.RefundResponse;
import com.szs.sungsu.domain.Member;
import com.szs.sungsu.domain.ScrapStatus;
import com.szs.sungsu.domain.Tax;
import com.szs.sungsu.exception.MemberException;
import com.szs.sungsu.repository.MemberRepository;
import com.szs.sungsu.repository.TaxJpaRepository;
import com.szs.sungsu.repository.TaxRepository;
import com.szs.sungsu.service.dto.RemoteCallResultDto;
import com.szs.sungsu.util.TwoWayEncryptUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@Transactional(readOnly = true)
public class TaxService {

    public static final String API_URL = "https://codetest.3o3.co.kr/v2/scrap";

    private final TaxRepository taxRepository;
    private final TaxJpaRepository taxJPARepository;
    private final MemberRepository memberRepository;
    private final RestClient restClient;
    private static final ObjectMapper om = new ObjectMapper();

    public TaxService(TaxRepository taxRepository, TaxJpaRepository taxJPARepository, MemberRepository memberRepository) {
        this.taxRepository = taxRepository;
        this.taxJPARepository = taxJPARepository;
        this.memberRepository = memberRepository;

        // RestClient 연결 설정
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withReadTimeout(Duration.ofSeconds(20));
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactories.get(settings);
        this.restClient = RestClient.builder()
                .baseUrl(API_URL)
                .requestFactory(requestFactory)
                .build();
    }

    // 스크랩 진행중 상태 변경
    @Transactional
    public void preScrap(String userId) {

        // 1 select Member
        Optional<Member> maybeMember = memberRepository.findFirstByUserIdEquals(userId);
        if (maybeMember.isEmpty()) {
            log.warn("존재하지 않는 userId={}",userId);
            throw new MemberException("유저 정보가 올바르지 않습니다.");
        }

        // 준영속 tax
        Tax tax = Tax.createTax(maybeMember.get());

        // tax 영속화, 저장
        taxRepository.save(tax);
    }


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scrap(String userId) {
        // 1 select tax fetch join member
        Tax tax = taxJPARepository.findTaxByUserId(userId);

        // 주민번호 복호화
        String decryptedRegNo;
        try {
            decryptedRegNo = TwoWayEncryptUtil.decrypt(tax.getMember().getRegNo());
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage(), e);
            return;
        }

        // 외부 API 호출 실행
        ResponseEntity<String> responseEntity = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new RemoteCallRequest(tax.getMember().getName(), decryptedRegNo))
                .retrieve()
                .toEntity(String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful() ||
                !responseEntity.hasBody()) {
            /*
             NOTE(sss) 호출 실패 케이스,
             재시도 로직을 넣거나
             별도 배치를 통해서 상태가 진행중인데 오래된 데이터는 다시 시도하도록 해야함
             */
            log.error("scrap remote api call fail, userId={}", userId);
            throw new IllegalStateException();
        }

        // 응답 결과 json 파싱
        RemoteCallResultDto resultDto = parseScrap(responseEntity.getBody());

        // 1 update Tax by dirty check
        Tax.scrapedTax(tax,
                resultDto.getTotalPay(),
                resultDto.getCalculatedTax(),
                resultDto.getInsurance(),
                resultDto.getEducation(),
                resultDto.getDonation(),
                resultDto.getMedical(),
                resultDto.getRetirement());
    }

    public RemoteCallResultDto parseScrap(String jsonStr) {
        RemoteCallResultDto resultDto = new RemoteCallResultDto();
        try {
            JsonNode rootNode = om.readTree(jsonStr);
            if (!rootNode.get("status").asText("").equals("success")) {
                // 외부 호출 결과 실패. 그대로 끝내기
                log.error("scrap remote api call fail, responseBody={}", jsonStr);
                throw new IllegalStateException();
            }

            JsonNode dataNode = rootNode.get("data");

            JsonNode jsonListNode = dataNode.get("jsonList");

            // 급여
            JsonNode payListNode = jsonListNode.get("급여");
            resultDto.setTotalPay(StreamSupport.stream(payListNode.spliterator(), false)
                    .map(payNode -> new BigDecimal(payNode.get("총지급액").asText("0").replaceAll(",","")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            // 산출세액
            JsonNode calculatedTaxNode = jsonListNode.get("산출세액");
            resultDto.setCalculatedTax(
                    new BigDecimal(calculatedTaxNode.asText("0").replaceAll(",", "")));

            // 소득공제
            JsonNode incomeDeductionNode = jsonListNode.get("소득공제");
            Iterator<JsonNode> iter = incomeDeductionNode.elements();
            while (iter.hasNext()) {
                JsonNode node = iter.next();
                String condKey = node.get("소득구분").asText("");
                BigDecimal value = incomeDeductionNodeParser(node, condKey);
                switch (condKey) {
                    case "보험료" -> resultDto.setInsurance(value);
                    case "교육비" -> resultDto.setEducation(value);
                    case "기부금" -> resultDto.setDonation(value);
                    case "의료비" -> resultDto.setMedical(value);
                    case "퇴직연금" -> resultDto.setRetirement(value);
                }
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return resultDto;
    }

    // 소득공제 파싱
    private static BigDecimal incomeDeductionNodeParser(JsonNode node, String condKey) {
        String key = condKey.equals("퇴직연금") ? "총납임금액" : "금액";
        return new BigDecimal(node.get(key).asText("0").replaceAll(",", ""));
    }

    // 결정 세액 조회
    public RefundResponse refund(String userId) {

        // 1 select tax by fetch join member
        Tax tax = taxJPARepository.findTaxByUserId(userId);

        if (tax.getScrapStatus().equals(ScrapStatus.INPROGRESS)) {
            throw new MemberException("조회가 진행중입니다, 나중에 다시 시도해주세요");
        }

        // 계산 로직
        Pair<BigDecimal, BigDecimal> pair = calculateTax(tax);

        RefundResponse response = new RefundResponse();
        // NOTE(sss) 연산한 마지막 결과의 소수점 버림 정책으로 임의 설정 하였습니다
        response.setDeterminedTax(String.format("%,.0f", pair.getFirst().setScale(0, RoundingMode.DOWN)));
        response.setRetirement(String.format("%,.0f", pair.getSecond().setScale(0, RoundingMode.DOWN)));
        response.setName(tax.getMember().getName());

        return response;
    }

    // 결정세액 계산
    public Pair<BigDecimal, BigDecimal> calculateTax(Tax tax) {
        //근로소득공제금액
        BigDecimal 근로소득공제금액 = tax.getCalculatedTax().multiply(new BigDecimal("0.55"));

        //보험료공제금액
        BigDecimal 보험료공제금액 = tax.getInsurance().multiply(new BigDecimal("0.12"));
        //의료비공제금액
        BigDecimal 의료비공제금액 = tax.getMedical()
                .subtract(tax.getTotalPayment().multiply(new BigDecimal("0.03")))
                .multiply(new BigDecimal("0.15"))
                .max(new BigDecimal(0));
        //교육비공제금액
        BigDecimal 교육비공제금액 = tax.getEducation().multiply(new BigDecimal("0.15"));
        //기부금공제금액
        BigDecimal 기부금공제금액 = tax.getDonation().multiply(new BigDecimal("0.15"));
        //특별세액공제금액
        BigDecimal 특별세액공제금액 = 보험료공제금액.add(의료비공제금액).add(교육비공제금액).add(기부금공제금액);

        //표준세액공제금액
        BigDecimal 표준세액공제금액 = new BigDecimal("0");
        if (특별세액공제금액.compareTo(new BigDecimal("130000")) < 0) {
            표준세액공제금액 = new BigDecimal("130000");
            특별세액공제금액 = new BigDecimal("0");
        }

        //퇴직연금세액공제금액
        BigDecimal 퇴직연금세액공제금액 = tax.getRetirement().multiply(new BigDecimal("0.15"));

        BigDecimal 결정세액 = tax.getCalculatedTax()
                .subtract(근로소득공제금액)
                .subtract(특별세액공제금액)
                .subtract(표준세액공제금액)
                .subtract(퇴직연금세액공제금액)
                .max(new BigDecimal(0));

        return Pair.of(결정세액, 퇴직연금세액공제금액);
    }

    // 외부 API 호출 Request
    @Data
    @AllArgsConstructor
    static class RemoteCallRequest {
        String name;
        String regNo;
    }
}
