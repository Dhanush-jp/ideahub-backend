package com.ideahub.backend.repository;

import com.ideahub.backend.model.Follow;
import com.ideahub.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    List<Follow> findByFollower(User follower);

    long countByFollowing(User following);
}
