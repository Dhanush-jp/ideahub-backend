package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.idea.PagedIdeaResponse;
import com.ideahub.backend.service.IdeaService;
import com.ideahub.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final IdeaService ideaService;

    /**
     * Global feed of all ideas visible to the current user:
     * - all PUBLIC ideas from any author
     * - PRIVATE ideas authored by the current user
     */
    @GetMapping({"/posts", "/api/posts"})
    public ResponseEntity<ApiResponse<PagedIdeaResponse>> getPosts(@RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.getGlobalFeed(SecurityUtils.getCurrentUserId(), page, size)));
    }
}
