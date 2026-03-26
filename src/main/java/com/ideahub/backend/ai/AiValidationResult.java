package com.ideahub.backend.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiValidationResult {
    private final String marketPotential;
    private final String targetAudience;
    private final String riskLevel;
    private final int innovationScore;
    private final String suggestions;
    private final String feedback;
}
