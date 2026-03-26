package com.ideahub.backend.dto.interaction;

import com.ideahub.backend.model.ReactionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReactionRequest {
    @NotNull
    @Min(1)
    private Long ideaId;

    @NotNull
    private ReactionType type;
}
