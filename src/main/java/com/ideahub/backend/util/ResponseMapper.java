package com.ideahub.backend.util;

import com.ideahub.backend.dto.idea.IdeaResponse;
import com.ideahub.backend.dto.interaction.CommentResponse;
import com.ideahub.backend.dto.user.UserProfileResponse;
import com.ideahub.backend.model.Comment;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.User;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static IdeaResponse toIdeaResponse(Idea idea,
                                              long likesCount,
                                              long reactionsCount,
                                              long commentsCount,
                                              long savesCount,
                                              boolean reactedByCurrentUser,
                                              boolean savedByCurrentUser) {
        return IdeaResponse.builder()
                .id(idea.getId())
                .title(idea.getTitle())
                .description(idea.getDescription())
                .visibility(idea.getVisibility())
                .authorId(idea.getAuthor().getId())
                .authorUsername(idea.getAuthor().getUsername())
                .authorAvatarUrl(idea.getAuthor().getAvatarUrl())
                .likesCount(likesCount)
                .reactionsCount(reactionsCount)
                .commentsCount(commentsCount)
                .savesCount(savesCount)
                .reactedByCurrentUser(reactedByCurrentUser)
                .savedByCurrentUser(savedByCurrentUser)
                .aiScore(idea.getAiScore())
                .aiFeedback(idea.getAiFeedback())
                .aiSuggestions(idea.getAiSuggestions())
                .createdAt(idea.getCreatedAt())
                .build();
    }

    public static IdeaResponse toIdeaResponse(Idea idea) {
        return toIdeaResponse(idea, 0L, 0L, 0L, 0L, false, false);
    }

    public static CommentResponse toCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .ideaId(comment.getIdea().getId())
                .content(comment.getContent())
                .authorId(comment.getAuthor().getId())
                .authorUsername(comment.getAuthor().getUsername())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static UserProfileResponse toUserProfileResponse(User user, long followersCount, long ideasCount) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .followersCount(followersCount)
                .ideasCount(ideasCount)
                .build();
    }

    public static UserProfileResponse toUserProfileResponse(User user) {
        return toUserProfileResponse(user, 0L, 0L);
    }
}
