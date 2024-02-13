package com.szs.sungsu.api;

import com.szs.sungsu.api.response.RefundResponse;
import com.szs.sungsu.config.jwt.JwtTokenProvider;
import com.szs.sungsu.exception.CustomJwtException;
import com.szs.sungsu.service.TaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @Operation(summary = "스크래핑")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "스크랩 성공", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "스크랩 시작 실패", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "인증 실패", content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<?> scrap(HttpServletRequest request) {

        Optional<String> maybeUserId = tokenProvider.getUserIdByToken(request);
        if (maybeUserId.isEmpty()) {
            throw new CustomJwtException("토큰정보가 올바르지 않습니다.");
        }

        // 상태 변경
        taxService.preScrap(maybeUserId.get());

        // 외부 API 호출 실행
        taxService.scrap(maybeUserId.get());

        // 200 ok
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/szs/refund")
    @Operation(summary = "결정세액 조회")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = {@Content(schema = @Schema(implementation = RefundResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "400", description = "조회 진행중", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", description = "인증 실패", content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<RefundResponse> refund(HttpServletRequest request) {

        Optional<String> maybeUserId = tokenProvider.getUserIdByToken(request);
        if (maybeUserId.isEmpty()) {
            throw new CustomJwtException("토큰정보가 올바르지 않습니다.");
        }

        RefundResponse refundResponse = taxService.refund(maybeUserId.get());
        return new ResponseEntity<>(refundResponse, HttpStatus.OK);
    }
}
