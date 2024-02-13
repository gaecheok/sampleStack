package com.szs.sungsu.api;

import com.szs.sungsu.api.request.LoginRequest;
import com.szs.sungsu.api.request.SignupRequest;
import com.szs.sungsu.api.response.LoginResponse;
import com.szs.sungsu.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/szs/signup")
    @Operation(summary = "회원가입")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "201", description = "가입 완료", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "400", description = "가입 실패", content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {

        memberService.joinMember(
                req.getUserId(),
                req.getPassword(),
                req.getName(),
                req.getRegNo());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/szs/login")
    @Operation(summary = "로그인")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = {@Content(schema = @Schema(implementation = LoginResponse.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "403", description = "로그인 실패", content = {@Content(schema = @Schema())})
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {

        String accessToken = memberService.jwtLogin(req.getUserId(), req.getPassword());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
