package com.smartstudy.gateway.filter;

import com.smartstudy.common.security.JwtUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthFilterTest {

    private final JwtAuthFilter filter = new JwtAuthFilter();

    @Test
    void rejectsProtectedRequestWithoutBearerToken() {
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        ServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/notes").build());

        StepVerifier.create(filter.filter(exchange, markInvoked(chainInvoked)))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chainInvoked.get()).isFalse();
    }

    @Test
    void allowsWhitelistedAuthEndpointWithoutBearerToken() {
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        ServerWebExchange exchange = exchange(MockServerHttpRequest.post("/api/auth/login").build());

        StepVerifier.create(filter.filter(exchange, markInvoked(chainInvoked)))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(chainInvoked.get()).isTrue();
    }

    @Test
    void allowsProtectedRequestWithBearerToken() {
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        ServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/notes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + JwtUtils.generateToken("student"))
                .build());

        StepVerifier.create(filter.filter(exchange, markInvoked(chainInvoked)))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(chainInvoked.get()).isTrue();
    }

    @Test
    void rejectsProtectedRequestWithInvalidBearerToken() {
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        ServerWebExchange exchange = exchange(MockServerHttpRequest.get("/api/notes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-valid-token")
                .build());

        StepVerifier.create(filter.filter(exchange, markInvoked(chainInvoked)))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(chainInvoked.get()).isFalse();
    }

    private ServerWebExchange exchange(MockServerHttpRequest request) {
        return MockServerWebExchange.from(request);
    }

    private WebFilterChain markInvoked(AtomicBoolean chainInvoked) {
        return exchange -> {
            chainInvoked.set(true);
            return Mono.empty();
        };
    }
}
