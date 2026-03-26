package com.ideahub.backend.dto.idea;

import com.ideahub.backend.model.IdeaVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdeaUpdateRequest {
    @NotBlank
    @Size(min = 5, max = 140)
    private String title;

    @NotBlank
    @Size(min = 20, max = 10000)
    private String description;

    @NotNull
    private IdeaVisibility visibility;
}
