package com.ideahub.backend.repository;

import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.SavedIdea;
import com.ideahub.backend.model.User;
import com.ideahub.backend.util.IdeaCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SavedIdeaRepository extends JpaRepository<SavedIdea, Long> {
    Optional<SavedIdea> findByUserAndIdea(User user, Idea idea);

    List<SavedIdea> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            select s.idea.id as ideaId, count(s.id) as total
            from SavedIdea s
            where s.idea.id in :ideaIds
            group by s.idea.id
            """)
    List<IdeaCountProjection> countByIdeaIds(@Param("ideaIds") Collection<Long> ideaIds);
}
