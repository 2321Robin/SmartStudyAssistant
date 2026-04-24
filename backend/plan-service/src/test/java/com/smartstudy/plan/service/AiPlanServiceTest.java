package com.smartstudy.plan.service;

import com.smartstudy.plan.entity.StudyTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class AiPlanServiceTest {

    @Test
    void generateTasksWithoutApiKeyFallsBackToTemplateStages(CapturedOutput output) {
        AiPlanService aiPlanService = new AiPlanService(new RestTemplateBuilder(), "", "https://api.deepseek.com", "deepseek-chat");

        List<StudyTask> tasks = aiPlanService.generateTasks("30天学完Spring Cloud", 30, 2);

        assertThat(tasks).hasSize(4);
        assertThat(tasks)
                .extracting(StudyTask::getTitle)
                .containsExactly("阶段一：基础准备", "阶段二：核心学习", "阶段三：项目实践", "阶段四：总结复习");
        assertThat(output).contains("Plan generation source=fallback-template reason=api key missing model=deepseek-chat");
    }

    @Test
    void generateTasksUsesDeepSeekWhenApiCallSucceeds(CapturedOutput output) {
        RecordingRestTemplate restTemplate = new RecordingRestTemplate();
        restTemplate.responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\\"tasks\\\":[{\\\"title\\\":\\\"阶段一：理解微服务基础\\\",\\\"stage\\\":1},{\\\"title\\\":\\\"阶段二：完成服务拆分\\\",\\\"stage\\\":2},{\\\"title\\\":\\\"阶段三：联调网关与认证\\\",\\\"stage\\\":3},{\\\"title\\\":\\\"阶段四：整理答辩演示\\\",\\\"stage\\\":4}]}"
                      }
                    }
                  ]
                }
                """;
        AiPlanService aiPlanService = new AiPlanService(restTemplate, "test-key", "https://api.deepseek.com", "deepseek-chat");

        List<StudyTask> tasks = aiPlanService.generateTasks("30天学完Spring Cloud", 30, 2);

        assertThat(restTemplate.lastUrl).isEqualTo("https://api.deepseek.com/chat/completions");
        assertThat(restTemplate.lastRequest).isInstanceOf(HttpEntity.class);
        HttpEntity<?> requestEntity = restTemplate.lastRequest;
        assertThat(requestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test-key");
        assertThat(tasks)
                .extracting(StudyTask::getTitle)
                .containsExactly("阶段一：理解微服务基础", "阶段二：完成服务拆分", "阶段三：联调网关与认证", "阶段四：整理答辩演示");
        assertThat(output).contains("Plan generation source=deepseek model=deepseek-chat goal=30天学完Spring Cloud");
    }

    @Test
    void generateTasksFallsBackToTemplateWhenApiCallFails() {
        RecordingRestTemplate restTemplate = new RecordingRestTemplate();
        restTemplate.exception = new RestClientException("deepseek unavailable");
        AiPlanService aiPlanService = new AiPlanService(restTemplate, "test-key", "https://api.deepseek.com", "deepseek-chat");

        List<StudyTask> tasks = aiPlanService.generateTasks("30天学完Spring Cloud", 30, 2);

        assertThat(tasks)
                .extracting(StudyTask::getTitle)
                .containsExactly("阶段一：基础准备", "阶段二：核心学习", "阶段三：项目实践", "阶段四：总结复习");
    }

    private static final class RecordingRestTemplate extends RestTemplate {

        private String lastUrl;
        private HttpEntity<?> lastRequest;
        private String responseBody;
        private RestClientException exception;

        @Override
        public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables)
                throws RestClientException {
            this.lastUrl = url;
            this.lastRequest = (HttpEntity<?>) request;

            if (exception != null) {
                throw exception;
            }

            return responseType.cast(responseBody);
        }
    }
}
