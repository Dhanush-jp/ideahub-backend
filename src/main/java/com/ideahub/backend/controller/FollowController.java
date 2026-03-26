package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.ApiMessageResponse;
import com.ideahub.backend.dto.interaction.FollowRequest;
import com.ideahub.backend.service.FollowService;
import com.ideahub.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> follow(@Valid @RequestBody FollowRequest request) {
        followService.follow(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Followed user").build()));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> unfollow(@RequestParam Long userId) {
        followService.unfollow(SecurityUtils.getCurrentUserId(), userId);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Unfollowed user").build()));
    }
}
