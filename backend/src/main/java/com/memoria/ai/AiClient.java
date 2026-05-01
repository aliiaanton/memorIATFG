package com.memoria.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class AiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiClient.class);

    private final RestClient restClient;

    public AiClient(RestClient.Builder builder, @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.restClient = builder.baseUrl(aiServiceUrl).build();
    }

    public String generateResponse(AiPromptRequest request) {
        try {
            AiPromptResponse response = restClient.post()
                    .uri("/generate-response")
                    .body(request)
                    .retrieve()
                    .body(AiPromptResponse.class);

            if (response != null && response.text() != null && !response.text().isBlank()) {
                return response.text();
            }
        } catch (RestClientResponseException exception) {
            LOGGER.warn("AI service request failed with status {}. Response body: {}",
                    exception.getStatusCode(), exception.getResponseBodyAsString());
        } catch (RestClientException ignored) {
            LOGGER.warn("AI service request failed: {}", ignored.getMessage());
        }

        return fallbackResponse(request.patientName());
    }

    private String fallbackResponse(String patientName) {
        String name = patientName == null || patientName.isBlank() ? "" : ", " + patientName;
        return "Te entiendo" + name + ". Vamos a hablar de algo tranquilo y agradable.";
    }
}
