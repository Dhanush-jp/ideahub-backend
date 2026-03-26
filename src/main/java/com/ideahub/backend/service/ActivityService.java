package com.ideahub.backend.service;

import com.ideahub.backend.dto.activity.ActivityResponse;
import com.ideahub.backend.dto.idea.IdeaResponse;
import com.ideahub.backend.dto.interaction.CommentResponse;
import com.ideahub.backend.model.Comment;
import com.ideahub.backend.model.Reaction;
import com.ideahub.backend.model.SavedIdea;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.CommentRepository;
import com.ideahub.backend.repository.ReactionRepository;
import com.ideahub.backend.repository.SavedIdeaRepository;
import com.ideahub.backend.util.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final SavedIdeaRepository savedIdeaRepository;
    private final UserService userService;
    private final IdeaService ideaService;

    @Transactional(readOnly = true)
    public ActivityResponse getActivity(Long currentUserId) {
        User user = userService.getUserEntity(currentUserId);

        List<Reaction> reactions = reactionRepository.findByUserOrderByCreatedAtDesc(user);
        List<Comment> comments = commentRepository.findByAuthorOrderByCreatedAtDesc(user);
        List<SavedIdea> savedIdeas = savedIdeaRepository.findByUserOrderByCreatedAtDesc(user);

        Map<Long, IdeaResponse> likedIdeaMap = new LinkedHashMap<>();
        for (Reaction reaction : reactions) {
            IdeaResponse response = ideaService.getIdeaById(currentUserId, reaction.getIdea().getId());
            likedIdeaMap.putIfAbsent(response.getId(), response);
        }

        List<CommentResponse> commented = comments.stream()
                .map(ResponseMapper::toCommentResponse)
                .toList();

        List<IdeaResponse> saved = savedIdeas.stream()
                .map(savedIdea -> ideaService.getIdeaById(currentUserId, savedIdea.getIdea().getId()))
                .toList();

        return ActivityResponse.builder()
                .likedIdeas(List.copyOf(likedIdeaMap.values()))
                .commentedIdeas(commented)
                .savedIdeas(saved)
                .build();
    }
}
