package com.szs.sungsu.api;

import com.szs.sungsu.api.response.RefundResponse;
import com.szs.sungsu.config.JwtTokenProvider;
import com.szs.sungsu.service.TaxService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TaxController {

    private final JwtTokenProvider tokenProvider;
    private final TaxService taxService;

    @PostMapping("/szs/scrap")
    public ResponseEntity<?> scrap(HttpServletRequest request) {

        Optional<String> maybeUserId = tokenProvider.getUserIdByToken(request);
        if (maybeUserId.isEmpty()) {
            throw new IllegalStateException("토큰에 문제가 있습니다.");
        }

        // 상태 변경
        taxService.preScrap(maybeUserId.get());

        // 외부 API 호출 실행
        taxService.scrap(maybeUserId.get());

        // 200 ok
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/szs/refund")
    public ResponseEntity<RefundResponse> refund(HttpServletRequest request) {

        Optional<String> maybeUserId = tokenProvider.getUserIdByToken(request);
        if (maybeUserId.isEmpty()) {
            throw new IllegalStateException("토큰에 문제가 있습니다.");
        }

        RefundResponse refundResponse = taxService.refund(maybeUserId.get());
        return new ResponseEntity<>(refundResponse, HttpStatus.OK);
    }
}
