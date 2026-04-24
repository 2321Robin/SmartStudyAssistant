package com.smartstudy.gateway.config;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRouteConfigTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void includesRequiredServiceRoutesWithExternalizedUris() {
        StepVerifier.create(routeDefinitionLocator.getRouteDefinitions().collectList())
                .assertNext(routeDefinitions -> {
                    Map<String, URI> routesById = routeDefinitions.stream()
                            .collect(Collectors.toMap(RouteDefinition::getId, RouteDefinition::getUri));

                    assertThat(routesById)
                            .containsKeys("user-service-route", "user-auth-route", "plan-service-route", "resource-service-route", "note-service-route");
                    assertThat(routesById.get("user-service-route")).isEqualTo(URI.create("http://user-service"));
                    assertThat(routesById.get("user-auth-route")).isEqualTo(URI.create("http://user-service"));
                    assertThat(routesById.get("plan-service-route")).isEqualTo(URI.create("http://plan-service"));
                    assertThat(routesById.get("resource-service-route")).isEqualTo(URI.create("http://resource-service"));
                    assertThat(routesById.get("note-service-route")).isEqualTo(URI.create("http://note-service"));

                    Map<String, String> routePredicatesById = routeDefinitions.stream()
                            .collect(Collectors.toMap(
                                    RouteDefinition::getId,
                                    route -> route.getPredicates().stream()
                                            .map(predicate -> predicate.getName() + predicate.getArgs().toString())
                                            .collect(Collectors.joining(","))
                            ));

                    assertThat(routePredicatesById.get("user-auth-route")).contains("/api/auth/**");
                })
                .verifyComplete();
    }
}
