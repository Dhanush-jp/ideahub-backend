package com.ideahub.backend.repository;

import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.Reaction;
import com.ideahub.backend.model.User;
import com.ideahub.backend.util.IdeaCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByIdeaAndUser(Idea idea, User user);

    List<Reaction> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
            select r.idea.id as ideaId, count(r.id) as total
            from Reaction r
            where r.idea.id in :ideaIds
            group by r.idea.id
            """)
    List<IdeaCountProjection> countByIdeaIds(@Param("ideaIds") Collection<Long> ideaIds);

    @Query("""
            select r.idea.id as ideaId, count(r.id) as total
            from Reaction r
            where r.idea.id in :ideaIds
              and r.type = com.ideahub.backend.model.ReactionType.LIKE
            group by r.idea.id
            """)
    List<IdeaCountProjection> countLikesByIdeaIds(@Param("ideaIds") Collection<Long> ideaIds);
}
