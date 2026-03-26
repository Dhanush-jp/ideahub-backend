package com.ideahub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private final Long id;
    private final String username;
    private final String email;
    private final String bio;
    private final String avatarUrl;
    private final long followersCount;
    private final long ideasCount;
}
