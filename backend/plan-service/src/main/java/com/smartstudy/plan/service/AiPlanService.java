package com.smartstudy.plan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstudy.plan.entity.StudyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiPlanService {

    private static final String DEFAULT_STATUS = "PENDING";
    private static final Logger log = LoggerFactory.getLogger(AiPlanService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String baseUrl;
    private final String model;

    @Autowired
    public AiPlanService(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}") String apiKey,
            @Value("${deepseek.base-url:https://api.deepseek.com}") String baseUrl,
            @Value("${deepseek.model:deepseek-chat}") String model) {
        this(
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(10))
                        .setReadTimeout(Duration.ofSeconds(20))
                        .build(),
                objectMapper,
                apiKey,
                baseUrl,
                model
        );
    }

    AiPlanService(RestTemplateBuilder restTemplateBuilder, String apiKey, String baseUrl, String model) {
        this(
                restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(10))
                        .setReadTimeout(Duration.ofSeconds(20))
                        .build(),
                new ObjectMapper(),
                apiKey,
                baseUrl,
                model
        );
    }

    AiPlanService(RestTemplate restTemplate, String apiKey, String baseUrl, String model) {
        this(restTemplate, new ObjectMapper(), apiKey, baseUrl, model);
    }

    AiPlanService(RestTemplate restTemplate, ObjectMapper objectMapper, String apiKey, String baseUrl, String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    public List<StudyTask> generateTasks(String goal, Integer durationDays, Integer dailyHours) {
        if (!StringUtils.hasText(apiKey)) {
            return fallbackTasks("api key missing");
        }

        try {
            List<StudyTask> aiTasks = requestDeepSeekTasks(goal, durationDays, dailyHours);
            if (aiTasks.isEmpty()) {
                return fallbackTasks("deepseek returned no tasks");
            }
            log.info("Plan generation source=deepseek model={} goal={}", model, goal);
            return aiTasks;
        } catch (Exception exception) {
            return fallbackTasks("deepseek request failed: " + exception.getClass().getSimpleName());
        }
    }

    private List<StudyTask> requestDeepSeekTasks(String goal, Integer durationDays, Integer dailyHours) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object"),
                "messages", List.of(
                        Map.of("role", "system", "content", "你是学习规划助手。请只返回 JSON 对象，不要返回 Markdown，不要解释。"),
                        Map.of("role", "user", "content", buildPrompt(goal, durationDays, dailyHours))
                )
        );

        String requestJson = objectMapper.writeValueAsString(requestBody);

        String responseBody = restTemplate.postForObject(
                normalizeBaseUrl(baseUrl) + "/chat/completions",
                new HttpEntity<>(requestJson, headers),
                String.class
        );

        if (!StringUtils.hasText(responseBody)) {
            return fallbackTasks("deepseek response body empty");
        }

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
        if (!contentNode.isTextual() || !StringUtils.hasText(contentNode.asText())) {
            return fallbackTasks("deepseek content missing");
        }

        JsonNode tasksNode = objectMapper.readTree(contentNode.asText()).path("tasks");
        if (!tasksNode.isArray() || tasksNode.isEmpty()) {
            return fallbackTasks("deepseek tasks missing");
        }

        List<StudyTask> tasks = new ArrayList<>();
        for (JsonNode taskNode : tasksNode) {
            String title = taskNode.path("title").asText();
            int stage = taskNode.path("stage").asInt(tasks.size() + 1);
            if (StringUtils.hasText(title)) {
                tasks.add(new StudyTask(null, title.trim(), stage, DEFAULT_STATUS));
            }
        }

        return tasks;
    }

    private String buildPrompt(String goal, Integer durationDays, Integer dailyHours) {
        return """
                请根据以下学习目标生成一个中文学习计划。
                只返回 JSON 对象，格式如下：
                {"tasks":[{"title":"阶段任务标题","stage":1},{"title":"阶段任务标题","stage":2}]}

                要求：
                1. 返回 4 到 6 个阶段任务
                2. 任务标题要具体、自然，适合课程项目演示
                3. stage 从 1 开始递增
                4. 不要输出除 JSON 以外的任何内容

                学习目标：%s
                计划天数：%d
                每日学习时长：%d 小时
                """.formatted(goal, durationDays, dailyHours);
    }

    private String normalizeBaseUrl(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private List<StudyTask> fallbackTasks(String reason) {
        log.info("Plan generation source=fallback-template reason={} model={} ", reason, model);
        return List.of(
                new StudyTask(null, "阶段一：基础准备", 1, DEFAULT_STATUS),
                new StudyTask(null, "阶段二：核心学习", 2, DEFAULT_STATUS),
                new StudyTask(null, "阶段三：项目实践", 3, DEFAULT_STATUS),
                new StudyTask(null, "阶段四：总结复习", 4, DEFAULT_STATUS)
        );
    }
}
