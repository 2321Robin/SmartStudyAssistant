package com.smartstudy.common.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class JwtUtils {

    private static final String PLACEHOLDER_PREFIX = "placeholder-token:";

    private JwtUtils() {
    }

    // This is an explicit placeholder token format until real JWT signing is added.
    public static String generateToken(String subject) {
        return PLACEHOLDER_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(subject.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isValid(String token) {
        return getSubject(token).isPresent();
    }

    public static Optional<String> getSubject(String token) {
        if (token == null || !token.startsWith(PLACEHOLDER_PREFIX)) {
            return Optional.empty();
        }

        String encodedSubject = token.substring(PLACEHOLDER_PREFIX.length());
        if (encodedSubject.isBlank()) {
            return Optional.empty();
        }

        try {
            String subject = new String(Base64.getUrlDecoder().decode(encodedSubject), StandardCharsets.UTF_8);
            return subject.isBlank() ? Optional.empty() : Optional.of(subject);
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
