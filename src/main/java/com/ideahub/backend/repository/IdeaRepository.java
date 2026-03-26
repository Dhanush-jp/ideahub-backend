package com.ideahub.backend.repository;

import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface IdeaRepository extends JpaRepository<Idea, Long> {

    boolean existsByAuthorAndTitleIgnoreCase(User author, String title);

    @Query("""
            select i from Idea i
            where i.visibility = com.ideahub.backend.model.IdeaVisibility.PUBLIC
               or i.author = :viewer
            """)
    Page<Idea> findVisibleIdeas(@Param("viewer") User viewer, Pageable pageable);

    @Query("""
            select i from Idea i
            where i.visibility = com.ideahub.backend.model.IdeaVisibility.PUBLIC
               or i.author = :viewer
            order by i.createdAt desc
            """)
    Page<Idea> findGlobalVisibleIdeas(@Param("viewer") User viewer, Pageable pageable);

    @Query("""
            select i from Idea i
            where i.author = :viewer
               or (i.author in :following and i.visibility = com.ideahub.backend.model.IdeaVisibility.PUBLIC)
            order by i.createdAt desc
            """)
    Page<Idea> findFeedIdeas(@Param("viewer") User viewer,
                             @Param("following") Collection<User> following,
                             Pageable pageable);

    /**
     * Trending ideas are ranked by LIKE reaction count, then by recency.
     * If all ideas have 0 likes, the secondary order-by (createdAt desc) guarantees deterministic results.
     */
    @Query("""
            select i from Idea i
            where i.visibility = com.ideahub.backend.model.IdeaVisibility.PUBLIC
               or i.author = :viewer
            order by
              (select count(r) from com.ideahub.backend.model.Reaction r
                 where r.idea = i and r.type = com.ideahub.backend.model.ReactionType.LIKE) desc,
              i.createdAt desc
            """)
    Page<Idea> findTrendingIdeas(@Param("viewer") User viewer, Pageable pageable);

    Page<Idea> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
}

