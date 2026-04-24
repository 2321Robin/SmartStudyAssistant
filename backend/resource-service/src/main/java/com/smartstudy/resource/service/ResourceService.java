package com.smartstudy.resource.service;

import com.smartstudy.resource.entity.LearningResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class ResourceService {

    private static final List<LearningResource> CATALOG = List.of(
            new LearningResource(
                    1L,
                    "Spring Cloud Alibaba 概览 / Spring Cloud Alibaba Overview",
                    "梳理 Spring Cloud Alibaba 的核心组件与常见使用场景，适合入门阶段快速建立整体认知。",
                    "Spring 官方",
                    "入门",
                    "ARTICLE",
                    List.of("Spring Cloud", "Microservice"),
                    "https://spring.io/projects/spring-cloud"
            ),
            new LearningResource(
                    2L,
                    "网关实战指南 / Gateway Practice Guide",
                    "聚焦网关路由、过滤器与统一入口设计，适合课程项目中快速搭建访问层。",
                    "Bilibili 课程",
                    "入门",
                    "VIDEO",
                    List.of("Gateway", "Architecture"),
                    "https://example.com/videos/gateway-practice"
            ),
            new LearningResource(
                    3L,
                    "Nacos 快速上手 / Nacos Quick Start",
                    "介绍服务注册、配置管理与本地启动流程，帮助理解微服务基础治理能力。",
                    "Nacos 官方",
                    "入门",
                    "ARTICLE",
                    List.of("Nacos", "Registry", "Config"),
                    "https://nacos.io/en/docs/latest/quickstart/quick-start/"
            ),
            new LearningResource(
                    4L,
                    "OpenFeign 服务调用示例 / OpenFeign Service Call Demo",
                    "展示服务间声明式调用的典型写法，便于串联多个业务服务的通信流程。",
                    "Spring 文档",
                    "进阶",
                    "ARTICLE",
                    List.of("OpenFeign", "Spring Cloud", "RPC"),
                    "https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/"
            ),
            new LearningResource(
                    5L,
                    "JWT 认证基础 / JWT Authentication Essentials",
                    "概括 JWT 的结构、认证流程与常见安全注意点，适合登录鉴权模块入门。",
                    "JWT 官方",
                    "入门",
                    "ARTICLE",
                    List.of("JWT", "Authentication", "Security"),
                    "https://jwt.io/introduction"
            ),
            new LearningResource(
                    6L,
                    "Spring Boot 的 Redis 缓存实践 / Redis Caching for Spring Boot",
                    "结合缓存场景说明 Redis 在提升查询性能中的作用，适合作为进阶优化资料。",
                    "实战视频",
                    "进阶",
                    "VIDEO",
                    List.of("Redis", "Cache", "Spring Boot"),
                    "https://example.com/videos/redis-spring-boot"
            ),
            new LearningResource(
                    7L,
                    "Docker Compose 部署笔记 / Docker Compose Deployment Notes",
                    "从服务编排角度说明本地部署步骤，方便项目联调和演示环境准备。",
                    "Docker 官方",
                    "进阶",
                    "DOCUMENT",
                    List.of("Docker", "Deployment", "Compose"),
                    "https://docs.docker.com/compose/"
            ),
            new LearningResource(
                    8L,
                    "Jenkins 流水线基础 / Jenkins Pipeline Basics",
                    "帮助理解持续集成流水线的核心概念，适合作业中 CI/CD 部分的补充学习。",
                    "Jenkins 官方",
                    "入门",
                    "VIDEO",
                    List.of("Jenkins", "CI/CD", "Pipeline"),
                    "https://www.jenkins.io/doc/book/pipeline/"
            ),
            new LearningResource(
                    9L,
                    "Vue 3 仪表盘实践 / Vue 3 Dashboard Practice",
                    "围绕前端页面组织、组件布局与交互展示，适合完善课程项目演示界面。",
                    "Vue 官方",
                    "入门",
                    "ARTICLE",
                    List.of("Vue 3", "Frontend", "Dashboard"),
                    "https://vuejs.org/guide/introduction.html"
            ),
            new LearningResource(
                    10L,
                    "微服务架构案例解析 / Microservice Architecture Case Study",
                    "从真实案例角度理解服务拆分与边界设计，为整体系统结构提供参考。",
                    "架构博客",
                    "进阶",
                    "DOCUMENT",
                    List.of("Microservice", "Architecture", "Case Study"),
                    "https://microservices.io/patterns/microservices.html"
            )
    );

    public List<LearningResource> search(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        if (normalizedKeyword.isBlank()) {
            return CATALOG;
        }

        return CATALOG.stream()
                .filter(resource -> matches(resource, normalizedKeyword))
                .toList();
    }

    private boolean matches(LearningResource resource, String keyword) {
        return resource.title().toLowerCase(Locale.ROOT).contains(keyword)
                || resource.type().toLowerCase(Locale.ROOT).contains(keyword)
                || resource.url().toLowerCase(Locale.ROOT).contains(keyword)
                || resource.tags().stream()
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .anyMatch(tag -> tag.contains(keyword));
    }
}
