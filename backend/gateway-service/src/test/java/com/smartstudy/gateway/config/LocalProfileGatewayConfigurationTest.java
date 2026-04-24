package com.smartstudy.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import com.smartstudy.gateway.GatewayApplication;

@SpringBootTest(classes = GatewayApplication.class)
@ActiveProfiles("local")
class LocalProfileGatewayConfigurationTest {

    @Autowired
    private Environment environment;

    @Test
    void localProfileRoutesGatewayToLocalhostServices() {
        assertThat(environment.getProperty("server.port"))
                .isEqualTo("8088");
        assertThat(environment.getProperty("smartstudy.services.user-service-uri"))
                .isEqualTo("http://localhost:8081");
        assertThat(environment.getProperty("smartstudy.services.plan-service-uri"))
                .isEqualTo("http://localhost:8082");
        assertThat(environment.getProperty("smartstudy.services.resource-service-uri"))
                .isEqualTo("http://localhost:8083");
        assertThat(environment.getProperty("smartstudy.services.note-service-uri"))
                .isEqualTo("http://localhost:8084");
    }
}
