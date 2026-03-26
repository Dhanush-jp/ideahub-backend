package com.ideahub.backend.service;

import com.ideahub.backend.dto.interaction.SaveIdeaRequest;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.SavedIdea;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.SavedIdeaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SavedIdeaService {

    private final SavedIdeaRepository savedIdeaRepository;
    private final UserService userService;
    private final IdeaService ideaService;

    @Transactional
    public void saveIdea(Long currentUserId, SaveIdeaRequest request) {
        User user = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(request.getIdeaId(), user);

        savedIdeaRepository.findByUserAndIdea(user, idea)
                .ifPresentOrElse(existing -> {
                }, () -> {
                    SavedIdea savedIdea = new SavedIdea();
                    savedIdea.setUser(user);
                    savedIdea.setIdea(idea);
                    savedIdeaRepository.save(savedIdea);
                });
    }

    @Transactional
    public void unsaveIdea(Long currentUserId, Long ideaId) {
        User user = userService.getUserEntity(currentUserId);
        Idea idea = ideaService.findReadableIdea(ideaId, user);

        savedIdeaRepository.findByUserAndIdea(user, idea)
                .ifPresent(savedIdeaRepository::delete);
    }
}
