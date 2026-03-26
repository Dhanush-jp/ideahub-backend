package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.ApiMessageResponse;
import com.ideahub.backend.dto.idea.IdeaCreateRequest;
import com.ideahub.backend.dto.idea.IdeaResponse;
import com.ideahub.backend.dto.idea.IdeaUpdateRequest;
import com.ideahub.backend.dto.idea.PagedIdeaResponse;
import com.ideahub.backend.service.IdeaService;
import com.ideahub.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
public class IdeaController {

    private final IdeaService ideaService;

    @PostMapping
    public ResponseEntity<ApiResponse<IdeaResponse>> createIdea(@Valid @RequestBody IdeaCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.createIdea(SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedIdeaResponse>> getIdeas(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.getIdeas(SecurityUtils.getCurrentUserId(), page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IdeaResponse>> getIdea(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.getIdeaById(SecurityUtils.getCurrentUserId(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IdeaResponse>> updateIdea(@PathVariable Long id, @Valid @RequestBody IdeaUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.updateIdea(SecurityUtils.getCurrentUserId(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiMessageResponse>> deleteIdea(@PathVariable Long id) {
        ideaService.deleteIdea(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageResponse.builder().message("Idea deleted successfully").build()));
    }
}
