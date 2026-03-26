package com.ideahub.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideahub.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HuggingFaceClient {

    private final ObjectMapper objectMapper;
    private final Environment environment;

    @Value("${app.ai.huggingface.base-url:https://api-inference.huggingface.co/models}")
    private String baseUrl;

    @Value("${app.ai.huggingface.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${app.ai.huggingface.read-timeout-ms:20000}")
    private int readTimeoutMs;

    public String generateText(String model, String prompt) {
        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadRequestException("HuggingFace API key is missing. Set HUGGINGFACE_API_KEY.");
        }

        String endpoint = baseUrl + "/" + model;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "inputs", prompt,
                "parameters", Map.of("max_new_tokens", 300, "temperature", 0.2, "return_full_text", false)
        );

        try {
            RestTemplate restTemplate = buildRestTemplate();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && !root.isEmpty() && root.get(0).has("generated_text")) {
                return root.get(0).get("generated_text").asText();
            }
            if (root.has("generated_text")) {
                return root.get("generated_text").asText();
            }
            if (root.has("error")) {
                throw new BadRequestException("HuggingFace returned an error: " + root.get("error").asText());
            }
            throw new BadRequestException("Unexpected HuggingFace response format");
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new BadRequestException("HuggingFace authorization failed");
            }
            throw new BadRequestException("HuggingFace API call failed with status " + ex.getStatusCode().value());
        } catch (RestClientException ex) {
            log.warn("HuggingFace request failed: {}", ex.getMessage());
            throw new BadRequestException("HuggingFace API call failed: " + ex.getMessage());
        } catch (Exception ex) {
            throw new BadRequestException("Unable to parse HuggingFace response");
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    private String resolveApiKey() {
        // Support multiple naming conventions; whichever exists first wins.
        String key = environment.getProperty("app.ai.huggingface.api-key");
        if (key != null && !key.isBlank()) return key;

        key = environment.getProperty("app.ai.huggingface.apiKey");
        if (key != null && !key.isBlank()) return key;

        key = environment.getProperty("app.ai.huggingface.api_key");
        if (key != null && !key.isBlank()) return key;

        key = environment.getProperty("HUGGINGFACE_TOKEN");
        if (key != null && !key.isBlank()) return key;

        return environment.getProperty("HUGGINGFACE_API_KEY");
    }
}
