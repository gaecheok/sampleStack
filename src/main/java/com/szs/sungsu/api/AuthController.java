package com.szs.sungsu.api;

import com.szs.sungsu.api.request.LoginRequest;
import com.szs.sungsu.api.request.SignupRequest;
import com.szs.sungsu.api.response.LoginResponse;
import com.szs.sungsu.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {

        memberService.joinMember(
                req.getUserId(),
                req.getPassword(),
                req.getName(),
                req.getRegNo());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/szs/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        String accessToken = memberService.jwtLogin(req.getUserId(), req.getPassword());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
