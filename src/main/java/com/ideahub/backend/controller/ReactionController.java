package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.ApiMessageResponse;
import com.ideahub.backend.dto.interaction.ReactionRequest;
import com.ideahub.backend.service.ReactionService;
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
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> react(@Valid @RequestBody ReactionRequest request) {
        reactionService.react(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Reaction recorded").build()));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> removeReaction(@RequestParam Long ideaId) {
        reactionService.removeReaction(SecurityUtils.getCurrentUserId(), ideaId);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Reaction removed").build()));
    }
}
