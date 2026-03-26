package com.ideahub.backend.dto.activity;

import com.ideahub.backend.dto.idea.IdeaResponse;
import com.ideahub.backend.dto.interaction.CommentResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActivityResponse {
    private final List<IdeaResponse> likedIdeas;
    private final List<CommentResponse> commentedIdeas;
    private final List<IdeaResponse> savedIdeas;
}
