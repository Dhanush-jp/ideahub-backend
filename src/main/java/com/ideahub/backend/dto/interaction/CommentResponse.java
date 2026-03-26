package com.ideahub.backend.dto.interaction;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class CommentResponse {
    private final Long id;
    private final Long ideaId;
    private final String content;
    private final Long authorId;
    private final String authorUsername;
    private final Instant createdAt;
}
