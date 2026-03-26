package com.ideahub.backend.service;

import com.ideahub.backend.dto.user.UserProfileResponse;
import com.ideahub.backend.exception.NotFoundException;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.FollowRepository;
import com.ideahub.backend.repository.IdeaRepository;
import com.ideahub.backend.repository.UserRepository;
import com.ideahub.backend.util.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final IdeaRepository ideaRepository;

    @Transactional(readOnly = true)
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = getUserEntity(userId);
        long followersCount = followRepository.countByFollowing(user);
        long ideasCount = ideaRepository.findByAuthorOrderByCreatedAtDesc(user, PageRequest.of(0, 1)).getTotalElements();
        return ResponseMapper.toUserProfileResponse(user, followersCount, ideasCount);
    }
}
