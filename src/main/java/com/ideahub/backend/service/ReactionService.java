package com.ideahub.backend.service;

import com.ideahub.backend.dto.interaction.ReactionRequest;
import com.ideahub.backend.exception.NotFoundException;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.Reaction;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final UserService userService;
    private final IdeaService ideaService;

    @Transactional
    public void react(Long currentUserId, ReactionRequest request) {
        User user = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(request.getIdeaId(), user);

        Reaction reaction = reactionRepository.findByIdeaAndUser(idea, user)
                .orElseGet(() -> {
                    Reaction r = new Reaction();
                    r.setIdea(idea);
                    r.setUser(user);
                    return r;
                });

        reaction.setType(request.getType());
        reactionRepository.save(reaction);
    }

    @Transactional
    public void removeReaction(Long currentUserId, Long ideaId) {
        User user = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(ideaId, user);

        Reaction reaction = reactionRepository.findByIdeaAndUser(idea, user)
                .orElseThrow(() -> new NotFoundException("Reaction not found"));
        reactionRepository.delete(reaction);
    }
}
