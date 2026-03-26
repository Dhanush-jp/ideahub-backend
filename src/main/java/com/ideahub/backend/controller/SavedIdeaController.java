package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.ApiMessageResponse;
import com.ideahub.backend.dto.interaction.SaveIdeaRequest;
import com.ideahub.backend.service.SavedIdeaService;
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
@RequestMapping("/api/saved-ideas")
@RequiredArgsConstructor
public class SavedIdeaController {

    private final SavedIdeaService savedIdeaService;

    @PostMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> save(@Valid @RequestBody SaveIdeaRequest request) {
        savedIdeaService.saveIdea(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Idea saved").build()));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<ApiMessageResponse>> unsave(@RequestParam Long ideaId) {
        savedIdeaService.unsaveIdea(SecurityUtils.getCurrentUserId(), ideaId);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Idea unsaved").build()));
    }
}
