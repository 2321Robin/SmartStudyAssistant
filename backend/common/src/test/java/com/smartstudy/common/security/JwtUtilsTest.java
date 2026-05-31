package com.smartstudy.common.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilsTest {

    @Test
    void generatedTokenContainsThreeJwtSegmentsAndSubject() {
        String token = JwtUtils.generateToken("alice");

        assertThat(token.split("\\.", -1)).hasSize(3);
        assertThat(JwtUtils.getSubject(token)).contains("alice");
        assertThat(JwtUtils.isValid(token)).isTrue();
    }

    @Test
    void tamperedPayloadIsRejected() {
        String token = JwtUtils.generateToken("alice");
        String[] parts = token.split("\\.", -1);
        char replacement = parts[1].charAt(0) == 'a' ? 'b' : 'a';
        String tamperedPayload = replacement + parts[1].substring(1);
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        assertThat(JwtUtils.getSubject(tamperedToken)).isEqualTo(Optional.empty());
        assertThat(JwtUtils.isValid(tamperedToken)).isFalse();
    }

    @Test
    void malformedTokenIsRejected() {
        assertThat(JwtUtils.isValid("legacy-token:YWxpY2U")).isFalse();
        assertThat(JwtUtils.isValid("not-a-token")).isFalse();
        assertThat(JwtUtils.isValid(null)).isFalse();
    }
}
