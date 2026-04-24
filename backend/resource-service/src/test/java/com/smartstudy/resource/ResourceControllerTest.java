package com.smartstudy.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchReturnsDeterministicResourcesForKeyword() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", "Spring Cloud"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Spring Cloud Alibaba 概览 / Spring Cloud Alibaba Overview"))
                .andExpect(jsonPath("$.data[0].description").value("梳理 Spring Cloud Alibaba 的核心组件与常见使用场景，适合入门阶段快速建立整体认知。"))
                .andExpect(jsonPath("$.data[0].source").value("Spring 官方"))
                .andExpect(jsonPath("$.data[0].level").value("入门"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("Spring Cloud"));
    }

    @Test
    void searchReturnsMatchingGatewayResourceForGatewayKeyword() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", "Gateway"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("网关实战指南 / Gateway Practice Guide"))
                .andExpect(jsonPath("$.data[0].tags[0]").value("Gateway"));
    }

    @Test
    void searchReturnsExpandedCatalogWhenKeywordIsBlank() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(10));
    }

    @Test
    void searchReturnsJwtResourceForJwtKeyword() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", "JWT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("JWT 认证基础 / JWT Authentication Essentials"));
    }

    @Test
    void searchReturnsChineseTitledResourceForChineseKeyword() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", "网关"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("网关实战指南 / Gateway Practice Guide"));
    }

    @Test
    void searchReturnsEmptyListForUnknownKeyword() throws Exception {
        mockMvc.perform(get("/api/resources").param("keyword", "GraphQL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
