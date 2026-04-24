package com.smartstudy.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class LocalProfileConfigurationTest {

    @Autowired
    private Environment environment;

    @Test
    void localProfileUsesFileBackedH2Datasource() {
        assertThat(environment.getProperty("spring.datasource.url"))
                .startsWith("jdbc:h2:file:");
        assertThat(environment.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.h2.Driver");
    }
}
