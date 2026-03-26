package com.ideahub.backend.service;

import com.ideahub.backend.dto.interaction.CommentCreateRequest;
import com.ideahub.backend.dto.interaction.CommentResponse;
import com.ideahub.backend.model.Comment;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.CommentRepository;
import com.ideahub.backend.util.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final IdeaService ideaService;

    @Transactional
    public CommentResponse createComment(Long currentUserId, CommentCreateRequest request) {
        User user = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(request.getIdeaId(), user);

        Comment comment = new Comment();
        comment.setIdea(idea);
        comment.setAuthor(user);
        comment.setContent(request.getContent().trim());

        return ResponseMapper.toCommentResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForIdea(Long currentUserId, Long ideaId) {
        User viewer = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(ideaId, viewer);
        return commentRepository.findByIdeaOrderByCreatedAtAsc(idea).stream()
                .map(ResponseMapper::toCommentResponse)
                .toList();
    }
}
