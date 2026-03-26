package com.ideahub.backend.dto.interaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {
    @NotNull
    @Min(1)
    private Long ideaId;

    @NotBlank
    @Size(min = 1, max = 1000)
    private String content;
}
