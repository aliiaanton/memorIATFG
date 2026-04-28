package com.memoria.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AiClient {

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
        } catch (RestClientException ignored) {
            // During early MVP development, the backend must stay usable even if the AI service is offline.
        }

        return fallbackResponse(request.patientName());
    }

    private String fallbackResponse(String patientName) {
        String name = patientName == null || patientName.isBlank() ? "" : ", " + patientName;
        return "Te entiendo" + name + ". Vamos a hablar de algo tranquilo y agradable.";
    }
}

