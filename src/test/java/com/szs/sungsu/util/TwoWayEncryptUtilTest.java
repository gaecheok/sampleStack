package com.szs.sungsu.util;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

@Slf4j
class TwoWayEncryptUtilTest {

    @Test
    public void 암호화() throws GeneralSecurityException {
        // given
        String input = "test";

        // when
        String encrypt = TwoWayEncryptUtil.encrypt(input);

        // then
        Assertions.assertThat(encrypt).isNotEmpty();
    }

    @Test
    public void 암복호화() throws GeneralSecurityException {
        // given
        String input = "test";

        // when
        String encrypt = TwoWayEncryptUtil.encrypt(input);
        String decrypt = TwoWayEncryptUtil.decrypt(encrypt);

        // then
        Assertions.assertThat(decrypt).isEqualTo(input);
    }
}