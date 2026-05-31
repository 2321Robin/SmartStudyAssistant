package com.smartstudy.common.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

public final class JwtUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String DEFAULT_SECRET = "smartstudy-local-demo-secret";
    private static final long TOKEN_TTL_SECONDS = 24 * 60 * 60;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private JwtUtils() {
    }

    public static String generateToken(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }

        long expiresAt = Instant.now().getEpochSecond() + TOKEN_TTL_SECONDS;
        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("{\"sub\":\"" + escapeJson(subject) + "\",\"exp\":" + expiresAt + "}");
        String signingInput = header + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    public static boolean isValid(String token) {
        return getSubject(token).isPresent();
    }

    public static Optional<String> getSubject(String token) {
        if (token == null) {
            return Optional.empty();
        }

        String[] parts = token.split("\\.", -1);
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            return Optional.empty();
        }

        String signingInput = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(signingInput), parts[2])) {
            return Optional.empty();
        }

        try {
            String payload = new String(BASE64_URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            String subject = extractStringClaim(payload, "sub");
            Long expiresAt = extractLongClaim(payload, "exp");
            if (subject == null || subject.isBlank() || expiresAt == null || expiresAt < Instant.now().getEpochSecond()) {
                return Optional.empty();
            }
            return Optional.of(subject);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String base64Url(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String sign(String signingInput) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secretBytes(), HMAC_SHA256));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign token", exception);
        }
    }

    private static byte[] secretBytes() {
        String configuredSecret = System.getenv("SMARTSTUDY_JWT_SECRET");
        String secret = configuredSecret == null || configuredSecret.isBlank() ? DEFAULT_SECRET : configuredSecret;
        return secret.getBytes(StandardCharsets.UTF_8);
    }

    private static boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    private static String extractStringClaim(String payload, String claim) {
        String prefix = "\"" + claim + "\":\"";
        int start = payload.indexOf(prefix);
        if (start < 0) {
            return null;
        }

        int valueStart = start + prefix.length();
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int index = valueStart; index < payload.length(); index++) {
            char current = payload.charAt(index);
            if (escaped) {
                value.append(current);
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '\"') {
                return value.toString();
            } else {
                value.append(current);
            }
        }
        return null;
    }

    private static Long extractLongClaim(String payload, String claim) {
        String prefix = "\"" + claim + "\":";
        int start = payload.indexOf(prefix);
        if (start < 0) {
            return null;
        }

        int valueStart = start + prefix.length();
        int valueEnd = valueStart;
        while (valueEnd < payload.length() && Character.isDigit(payload.charAt(valueEnd))) {
            valueEnd++;
        }
        if (valueEnd == valueStart) {
            return null;
        }

        try {
            return Long.parseLong(payload.substring(valueStart, valueEnd));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
