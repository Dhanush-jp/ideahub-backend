package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.interaction.CommentCreateRequest;
import com.ideahub.backend.dto.interaction.CommentResponse;
import com.ideahub.backend.service.CommentService;
import com.ideahub.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(@Valid @RequestBody CommentCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.createComment(SecurityUtils.getCurrentUserId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@RequestParam Long ideaId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getCommentsForIdea(SecurityUtils.getCurrentUserId(), ideaId)));
    }
}
