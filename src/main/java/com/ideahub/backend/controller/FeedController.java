package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.idea.PagedIdeaResponse;
import com.ideahub.backend.service.IdeaService;
import com.ideahub.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedController {

    private final IdeaService ideaService;

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<PagedIdeaResponse>> getFeed(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size) {
        // Global feed: visible ideas from all authors (not restricted to follow-graph).
        return ResponseEntity.ok(ApiResponse.success(ideaService.getGlobalFeed(SecurityUtils.getCurrentUserId(), page, size)));
    }

    @GetMapping("/trending")
    public ResponseEntity<ApiResponse<PagedIdeaResponse>> getTrending(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(ideaService.getTrending(SecurityUtils.getCurrentUserId(), page, size)));
    }
}
