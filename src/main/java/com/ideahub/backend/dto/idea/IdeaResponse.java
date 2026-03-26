package com.ideahub.backend.dto.idea;

import com.ideahub.backend.model.IdeaVisibility;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class IdeaResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final IdeaVisibility visibility;
    private final Long authorId;
    private final String authorUsername;
    private final String authorAvatarUrl;
    private final long likesCount;
    private final long reactionsCount;
    private final long commentsCount;
    private final long savesCount;
    private final boolean reactedByCurrentUser;
    private final boolean savedByCurrentUser;
    private final Integer aiScore;
    private final String aiFeedback;
    private final String aiSuggestions;
    private final Instant createdAt;
}
