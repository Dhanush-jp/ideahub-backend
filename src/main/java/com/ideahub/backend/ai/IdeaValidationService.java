package com.ideahub.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ideahub.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaValidationService {

    private final HuggingFaceClient huggingFaceClient;
    private final ObjectMapper objectMapper;

    @Value("${app.ai.huggingface.validation-model:google/flan-t5-large}")
    private String validationModel;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.mock-on-failure:true}")
    private boolean mockOnFailure;

    public AiValidationResult validateIdea(String description) {
        if (!aiEnabled) {
            return fallbackValidation(description);
        }

        String prompt = """
                You are a startup idea analyst. Return valid JSON only with keys:
                marketPotential, targetAudience, riskLevel, innovationScore, suggestions, feedback.
                innovationScore must be integer 0-100.
                Analyze idea description: %s
                """.formatted(description);

        try {
            String raw = huggingFaceClient.generateText(validationModel, prompt);
            JsonNode json = extractJsonNode(raw);

            int score = clampScore(json.path("innovationScore").asInt(50));
            return AiValidationResult.builder()
                    .marketPotential(readText(json, "marketPotential", "Unknown"))
                    .targetAudience(readText(json, "targetAudience", "General"))
                    .riskLevel(readText(json, "riskLevel", "Medium"))
                    .innovationScore(score)
                    .suggestions(readText(json, "suggestions", "Refine value proposition and validate customer demand."))
                    .feedback(readText(json, "feedback", "AI analysis completed"))
                    .build();
        } catch (BadRequestException ex) {
            if (!mockOnFailure) {
                throw ex;
            }
            log.warn("Falling back to local idea validation: {}", ex.getMessage());
            return fallbackValidation(description);
        }
    }

    private JsonNode extractJsonNode(String raw) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start < 0 || end <= start) {
                throw new BadRequestException("AI output did not contain valid JSON");
            }
            String jsonString = raw.substring(start, end + 1);
            return objectMapper.readTree(jsonString);
        } catch (Exception ex) {
            throw new BadRequestException("Failed to parse AI validation result");
        }
    }

    private String readText(JsonNode node, String key, String fallback) {
        String value = node.path(key).asText();
        return value == null || value.isBlank() ? fallback : value;
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private AiValidationResult fallbackValidation(String description) {
        int score = Math.max(35, Math.min(92, description == null ? 50 : description.trim().length() / 4));
        return AiValidationResult.builder()
                .marketPotential("Moderate")
                .targetAudience("Early adopters and startup-focused users")
                .riskLevel("Medium")
                .innovationScore(score)
                .suggestions("Clarify the target user, sharpen the core differentiator, and validate willingness to pay.")
                .feedback("Fallback validation was used because live AI validation was unavailable.")
                .build();
    }
}
