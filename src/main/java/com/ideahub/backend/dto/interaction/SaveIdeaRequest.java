package com.ideahub.backend.dto.interaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveIdeaRequest {
    @NotNull
    @Min(1)
    private Long ideaId;
}
