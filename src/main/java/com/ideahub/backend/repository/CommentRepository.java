package com.ideahub.backend.repository;

import com.ideahub.backend.model.Comment;
import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.User;
import com.ideahub.backend.util.IdeaCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIdeaOrderByCreatedAtAsc(Idea idea);

    List<Comment> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("""
            select c.idea.id as ideaId, count(c.id) as total
            from Comment c
            where c.idea.id in :ideaIds
            group by c.idea.id
            """)
    List<IdeaCountProjection> countByIdeaIds(@Param("ideaIds") Collection<Long> ideaIds);
}
