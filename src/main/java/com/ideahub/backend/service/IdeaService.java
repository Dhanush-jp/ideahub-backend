package com.ideahub.backend.service;

import com.ideahub.backend.ai.AiValidationResult;
import com.ideahub.backend.ai.IdeaValidationService;
import com.ideahub.backend.dto.idea.IdeaCreateRequest;
import com.ideahub.backend.dto.idea.IdeaResponse;
import com.ideahub.backend.dto.idea.IdeaUpdateRequest;
import com.ideahub.backend.dto.idea.PagedIdeaResponse;
import com.ideahub.backend.exception.ForbiddenException;
import com.ideahub.backend.exception.NotFoundException;
import com.ideahub.backend.model.Follow;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.IdeaVisibility;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.CommentRepository;
import com.ideahub.backend.repository.FollowRepository;
import com.ideahub.backend.repository.IdeaRepository;
import com.ideahub.backend.repository.ReactionRepository;
import com.ideahub.backend.repository.SavedIdeaRepository;
import com.ideahub.backend.util.IdeaCountProjection;
import com.ideahub.backend.util.ResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IdeaService {

    private final IdeaRepository ideaRepository;
    private final ReactionRepository reactionRepository;
    private final CommentRepository commentRepository;
    private final SavedIdeaRepository savedIdeaRepository;
    private final FollowRepository followRepository;
    private final UserService userService;
    private final IdeaValidationService ideaValidationService;

    @Transactional
    public IdeaResponse createIdea(Long currentUserId, IdeaCreateRequest request) {
        User author = userService.getUserEntity(currentUserId);

        Idea idea = new Idea();
        idea.setTitle(request.getTitle().trim());
        idea.setDescription(request.getDescription().trim());
        idea.setVisibility(request.getVisibility());
        idea.setAuthor(author);
        applyAiValidation(idea);

        Idea saved = ideaRepository.save(idea);
        return toIdeaResponse(
                saved,
                author,
                Map.of(), // likesCountMap
                Map.of(), // reactionCountMap
                Map.of(), // commentCountMap
                Map.of(), // saveCountMap
                Collections.emptySet(),
                Collections.emptySet()
        );
    }

    @Transactional
    public IdeaResponse updateIdea(Long currentUserId, Long ideaId, IdeaUpdateRequest request) {
        User currentUser = userService.getUserEntity(currentUserId);
        Idea idea = findReadableIdea(ideaId, currentUser);
        if (!idea.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only edit your own ideas");
        }

        idea.setTitle(request.getTitle().trim());
        idea.setDescription(request.getDescription().trim());
        idea.setVisibility(request.getVisibility());
        applyAiValidation(idea);

        Idea updated = ideaRepository.save(idea);
        return hydrateIdeas(List.of(updated), currentUser).get(0);
    }

    @Transactional
    public void deleteIdea(Long currentUserId, Long ideaId) {
        User currentUser = userService.getUserEntity(currentUserId);
        Idea idea = findReadableIdea(ideaId, currentUser);
        if (!idea.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own ideas");
        }
        ideaRepository.delete(idea);
    }

    @Transactional(readOnly = true)
    public PagedIdeaResponse getIdeas(Long currentUserId, int page, int size) {
        User currentUser = userService.getUserEntity(currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Idea> ideas = ideaRepository.findVisibleIdeas(currentUser, pageable);
        return toPageResponse(ideas, currentUser);
    }

    @Transactional(readOnly = true)
    public IdeaResponse getIdeaById(Long currentUserId, Long ideaId) {
        User currentUser = userService.getUserEntity(currentUserId);
        Idea idea = findReadableIdea(ideaId, currentUser);
        return hydrateIdeas(List.of(idea), currentUser).get(0);
    }

    @Transactional(readOnly = true)
    public PagedIdeaResponse getFeed(Long currentUserId, int page, int size) {
        User currentUser = userService.getUserEntity(currentUserId);
        List<User> following = followRepository.findByFollower(currentUser).stream()
                .map(Follow::getFollowing)
                .toList();

        Page<Idea> ideas = ideaRepository.findFeedIdeas(currentUser, following, PageRequest.of(page, size));
        return toPageResponse(ideas, currentUser);
    }

    /**
     * Global feed of all ideas visible to the current user:
     * - all PUBLIC ideas from any author
     * - PRIVATE ideas authored by the current user
     */
    @Transactional(readOnly = true)
    public PagedIdeaResponse getGlobalFeed(Long currentUserId, int page, int size) {
        User currentUser = userService.getUserEntity(currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Idea> ideas = ideaRepository.findGlobalVisibleIdeas(currentUser, pageable);
        return toPageResponse(ideas, currentUser);
    }

    @Transactional(readOnly = true)
    public PagedIdeaResponse getTrending(Long currentUserId, int page, int size) {
        User currentUser = userService.getUserEntity(currentUserId);
        Page<Idea> ideas = ideaRepository.findTrendingIdeas(currentUser, PageRequest.of(page, size));
        return toPageResponse(ideas, currentUser);
    }

    @Transactional(readOnly = true)
    public PagedIdeaResponse getIdeasByUser(Long currentUserId, Long userId, int page, int size) {
        User currentUser = userService.getUserEntity(currentUserId);
        User target = userService.getUserEntity(userId);

        Page<Idea> ideas = ideaRepository.findByAuthorOrderByCreatedAtDesc(target, PageRequest.of(page, size));
        List<Idea> filtered = ideas.stream()
                .filter(i -> i.getVisibility() == IdeaVisibility.PUBLIC || i.getAuthor().getId().equals(currentUser.getId()))
                .toList();

        return PagedIdeaResponse.builder()
                .items(hydrateIdeas(filtered, currentUser))
                .page(page)
                .size(size)
                .totalItems(filtered.size())
                .totalPages(1)
                .build();
    }

    @Transactional(readOnly = true)
    public Idea findReadableIdea(Long ideaId, User viewer) {
        Idea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new NotFoundException("Idea not found"));

        boolean canRead = idea.getVisibility() == IdeaVisibility.PUBLIC || idea.getAuthor().getId().equals(viewer.getId());
        if (!canRead) {
            throw new ForbiddenException("You do not have access to this idea");
        }
        return idea;
    }

    private PagedIdeaResponse toPageResponse(Page<Idea> page, User currentUser) {
        return PagedIdeaResponse.builder()
                .items(hydrateIdeas(page.getContent(), currentUser))
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private List<IdeaResponse> hydrateIdeas(List<Idea> ideas, User currentUser) {
        if (ideas.isEmpty()) {
            return List.of();
        }

        List<Long> ideaIds = ideas.stream().map(Idea::getId).toList();

        Map<Long, Long> reactionCountMap = toCountMap(reactionRepository.countByIdeaIds(ideaIds));
        Map<Long, Long> likesCountMap = toCountMap(reactionRepository.countLikesByIdeaIds(ideaIds));
        Map<Long, Long> commentCountMap = toCountMap(commentRepository.countByIdeaIds(ideaIds));
        Map<Long, Long> saveCountMap = toCountMap(savedIdeaRepository.countByIdeaIds(ideaIds));

        Set<Long> reactedIdeaIds = ideas.stream()
                .filter(i -> reactionRepository.findByIdeaAndUser(i, currentUser).isPresent())
                .map(Idea::getId)
                .collect(Collectors.toSet());

        Set<Long> savedIdeaIds = ideas.stream()
                .filter(i -> savedIdeaRepository.findByUserAndIdea(currentUser, i).isPresent())
                .map(Idea::getId)
                .collect(Collectors.toSet());

        return ideas.stream()
                .map(idea -> toIdeaResponse(idea, currentUser, likesCountMap, reactionCountMap, commentCountMap, saveCountMap, reactedIdeaIds, savedIdeaIds))
                .toList();
    }

    private IdeaResponse toIdeaResponse(Idea idea,
                                        User currentUser,
                                        Map<Long, Long> likesCountMap,
                                        Map<Long, Long> reactionCountMap,
                                        Map<Long, Long> commentCountMap,
                                        Map<Long, Long> saveCountMap,
                                        Set<Long> reactedIdeaIds,
                                        Set<Long> savedIdeaIds) {
        long reactionsCount = reactionCountMap.getOrDefault(idea.getId(), 0L);
        long likesCount = likesCountMap.getOrDefault(idea.getId(), 0L);
        long commentsCount = commentCountMap.getOrDefault(idea.getId(), 0L);
        long savesCount = saveCountMap.getOrDefault(idea.getId(), 0L);
        boolean reacted = reactedIdeaIds.contains(idea.getId());
        boolean saved = savedIdeaIds.contains(idea.getId());

        return ResponseMapper.toIdeaResponse(idea, likesCount, reactionsCount, commentsCount, savesCount, reacted, saved);
    }

    private Map<Long, Long> toCountMap(Collection<IdeaCountProjection> projections) {
        return projections.stream()
                .collect(Collectors.toMap(IdeaCountProjection::getIdeaId, IdeaCountProjection::getTotal, Long::sum));
    }

    private void applyAiValidation(Idea idea) {
        AiValidationResult validation = ideaValidationService.validateIdea(idea.getDescription());
        idea.setAiScore(validation.getInnovationScore());
        idea.setAiSuggestions(validation.getSuggestions());
        idea.setAiFeedback(
                "Market Potential: " + validation.getMarketPotential()
                        + "; Target Audience: " + validation.getTargetAudience()
                        + "; Risk Level: " + validation.getRiskLevel()
                        + "; Feedback: " + validation.getFeedback()
        );
    }
}
