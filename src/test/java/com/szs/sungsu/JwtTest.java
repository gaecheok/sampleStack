package com.szs.sungsu;

import com.szs.sungsu.config.jwt.JwtTokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

public class JwtTest {

    private final JwtTokenProvider tokenProvider;
    public JwtTest() {
        String secret = "qwertyuiop123456qwertyuiop123456qwertyuiop123456";
        Long expiration = 5000L;
        this.tokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    public void jwt_생성() {
        // given
        String userId = "hong";

        // when
        String token = tokenProvider.generateToken(userId);

        // then
        assertThat(token).isNotEmpty();
    }

    @Test
    public void jwt_검증() {
        // given
        String userId = "hong";

        // when
        String token = tokenProvider.generateToken(userId);

        // then
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    public void jwt_만료체크() throws InterruptedException {
        // given
        String userId = "hong";

        // when
        String token = tokenProvider.generateToken(userId);

        TimeUnit.MILLISECONDS.sleep(6000);

        assertThatThrownBy(() ->
                tokenProvider.isTokenExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
