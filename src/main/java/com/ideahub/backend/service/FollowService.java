package com.ideahub.backend.service;

import com.ideahub.backend.dto.interaction.FollowRequest;
import com.ideahub.backend.exception.BadRequestException;
import com.ideahub.backend.model.Follow;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public void follow(Long currentUserId, FollowRequest request) {
        if (currentUserId.equals(request.getUserId())) {
            throw new BadRequestException("You cannot follow yourself");
        }

        User follower = userService.getUserEntity(currentUserId);
        User following = userService.getUserEntity(request.getUserId());

        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresentOrElse(existing -> {
                }, () -> {
                    Follow follow = new Follow();
                    follow.setFollower(follower);
                    follow.setFollowing(following);
                    followRepository.save(follow);
                });
    }

    @Transactional
    public void unfollow(Long currentUserId, Long userId) {
        User follower = userService.getUserEntity(currentUserId);
        User following = userService.getUserEntity(userId);
        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(followRepository::delete);
    }
}
