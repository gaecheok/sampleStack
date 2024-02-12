package com.szs.sungsu.service;

import com.szs.sungsu.api.response.RefundResponse;
import com.szs.sungsu.domain.Member;
import com.szs.sungsu.domain.ScrapStatus;
import com.szs.sungsu.domain.Tax;
import com.szs.sungsu.repository.MemberRepository;
import com.szs.sungsu.repository.TaxJpaRepository;
import com.szs.sungsu.repository.TaxRepository;
import com.szs.sungsu.service.dto.RemoteCallResultDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class TaxServiceTest {

    @Autowired private EntityManager em;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TaxRepository taxRepository;
    @Autowired private TaxService taxService;

    @BeforeEach
    public void memberJoin() {
        Member member = new Member("hong", "1234", "홍길동", "860824-1655068");
        em.persist(member);
    }

    // 스크랩 준비
    @Test
    public void 스크랩준비_정상케이스() {
        // given
        String userId = "hong";
        Member member = getMember(userId);
        Optional<Tax> maybeTax = taxRepository.findFirstByMemberEquals(member);
        assertThat(maybeTax).isEmpty();

        // when
        // 외부 API 호출(스크랩) 전에 디비에 상태정보(진행중)와 함께 인서트
        taxService.preScrap(userId);
        Optional<Tax> targetTax = taxRepository.findFirstByMemberEquals(member);

        // then
        assertThat(targetTax).isNotEmpty();
        assertThat(targetTax.get().getMember().getUserId()).isEqualTo(userId);
        assertThat(targetTax.get().getScrapStatus()).isEqualTo(ScrapStatus.INPROGRESS);
    }

    @Test
    public void 스크랩준비_없는유저() {
        // given
        String userId = "blank_user";

        // when
        assertThatThrownBy(() -> taxService.preScrap(userId)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void 스크랩_json파싱테스트() {
        // given
        String responseBody = "{\"status\":\"success\",\"data\":{    \"jsonList\":{      \"급여\":[        {\"소득내역\":\"급여\",\"총지급액\":\"60,000,000\",\"업무시작일\":\"2020.10.02\",\"기업명\":\"(주)활빈당\",\"이름\":\"홍길동\",\"지급일\":\"2020.11.02\",\"업무종료일\":\"2021.11.02\",\"주민등록번호\":\"860824-1655068\",\"소득구분\":\"근로소득(연간)\",\"사업자등록번호\":\"012-34-56789\"}],      \"산출세액\":\"3,000,000\",      \"소득공제\":[        {\"금액\":\"100,000\",\"소득구분\":\"보험료\"},        {\"금액\":\"200,000\",\"소득구분\":\"교육비\"},        {\"금액\":\"150,000\",\"소득구분\":\"기부금\"},        {\"금액\":\"4,400,000\",\"소득구분\":\"의료비\"},        {\"총납임금액\":\"6,000,000\",\"소득구분\":\"퇴직연금\"}      ]},\"appVer\":\"2021112501\",\"errMsg\":\"\",\"company\":\"삼쩜삼\",\"svcCd\":\"test01\",\"hostNm\":\"jobis-codetest\",\"workerResDt\":\"2022-08-16T06:27:35.160789\",\"workerReqDt\":\"2022-08-16T06:27:35.160851\"},\"errors\":{}}";

        // when
        RemoteCallResultDto remoteCallResultDto = taxService.parseScrap(responseBody);

        // then
        assertThat(remoteCallResultDto.getTotalPay().compareTo(new BigDecimal("60000000"))).isEqualTo(0);
    }

    // 결정세액 조회
    @Test
    public void 결정세액조회_정상케이스() {
        // given
        String userId = "hong";
        Member member = getMember(userId);

        BigDecimal 총지급액 = new BigDecimal("60000000");
        BigDecimal 산출세액 = new BigDecimal("3000000");
        BigDecimal 보험료 = new BigDecimal("100000");
        BigDecimal 교육비 = new BigDecimal("200000");
        BigDecimal 기부금 = new BigDecimal("150000");
        BigDecimal 의료비 = new BigDecimal("4400000");
        BigDecimal 퇴직연금 = new BigDecimal("6000000");

        String expected_퇴직연금세액공제금액 = "900,000";
        String expected_결정세액 = "0";

        Tax tax = Tax.createTax(member);
        Tax.scrapTax(tax,
                총지급액,
                산출세액,
                보험료,
                교육비,
                기부금,
                의료비,
                퇴직연금);
        em.persist(tax);

        // when
        RefundResponse refundResponse = taxService.refund(userId);

        // then
        assertThat(refundResponse.getName()).isEqualTo(member.getName());
        assertThat(refundResponse.getDeterminedTax()).isEqualTo(expected_결정세액);
        assertThat(refundResponse.getRetirement()).isEqualTo(expected_퇴직연금세액공제금액);
    }

    @Test
    public void 결정세액조회_스크랩완료전케이스() {
        // given
        String userId = "hong";
        Member member = getMember(userId);

        Tax tax = Tax.createTax(member);
        em.persist(tax);

        // then
        assertThatThrownBy(() -> taxService.refund(userId)).isInstanceOf(IllegalStateException.class);
    }


    // 결정세액 계산
    @Test
    public void 결정세액계산_case1() {
        // given
        String userId = "hong";
        Member member = getMember(userId);

        BigDecimal 총지급액 = new BigDecimal("60000000");
        BigDecimal 산출세액 = new BigDecimal("3000000");
        BigDecimal 보험료 = new BigDecimal("100000");
        BigDecimal 교육비 = new BigDecimal("200000");
        BigDecimal 기부금 = new BigDecimal("150000");
        BigDecimal 의료비 = new BigDecimal("4400000");
        BigDecimal 퇴직연금 = new BigDecimal("6000000");

        BigDecimal expected_퇴직연금세액공제금액 = new BigDecimal("900000");
        BigDecimal expected_결정세액 = new BigDecimal("0");

        Tax tax = Tax.createTax(member);
        Tax.scrapTax(tax,
                총지급액,
                산출세액,
                보험료,
                교육비,
                기부금,
                의료비,
                퇴직연금);

        // when
        Pair<BigDecimal, BigDecimal> pair = taxService.calculateTax(tax);

        //then
        assertThat(pair.getFirst().compareTo(expected_결정세액)).isEqualTo(0);
        assertThat(pair.getSecond().compareTo(expected_퇴직연금세액공제금액)).isEqualTo(0);
    }

    @Test
    public void 결정세액계산_case2() {
        // given
        String userId = "hong";
        Member member = getMember(userId);

        BigDecimal 총지급액 = new BigDecimal("60000000");
        BigDecimal 산출세액 = new BigDecimal("3000000");
        BigDecimal 보험료 = new BigDecimal("100000");
        BigDecimal 교육비 = new BigDecimal("200000");
        BigDecimal 기부금 = new BigDecimal("150000");
        BigDecimal 의료비 = new BigDecimal("4400000");
        BigDecimal 퇴직연금 = new BigDecimal("2000000");

        BigDecimal 퇴직연금세액공제금액 = new BigDecimal("300000");
        BigDecimal 결정세액 = new BigDecimal("595500");

        Tax tax = Tax.createTax(member);
        Tax.scrapTax(tax,
                총지급액,
                산출세액,
                보험료,
                교육비,
                기부금,
                의료비,
                퇴직연금);

        // when
        Pair<BigDecimal, BigDecimal> pair = taxService.calculateTax(tax);

        //then
        assertThat(pair.getFirst().compareTo(결정세액)).isEqualTo(0);
        assertThat(pair.getSecond().compareTo(퇴직연금세액공제금액)).isEqualTo(0);
    }

    private Member getMember(String userId) {
        Optional<Member> maybeMember = memberRepository.findFirstByUserIdEquals(userId);
        assertThat(maybeMember).isNotEmpty();
        return maybeMember.get();
    }
}