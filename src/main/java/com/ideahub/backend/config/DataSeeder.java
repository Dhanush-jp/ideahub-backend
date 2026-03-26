package com.ideahub.backend.config;

import com.ideahub.backend.model.Idea;
import com.ideahub.backend.model.IdeaVisibility;
import com.ideahub.backend.model.Role;
import com.ideahub.backend.model.User;
import com.ideahub.backend.repository.IdeaRepository;
import com.ideahub.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final IdeaRepository ideaRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("IdeaHub sample data seeding disabled");
            return;
        }

        List<UserSeed> userSeeds = buildUserSeeds();
        List<IdeaSeed> ideaSeeds = buildIdeaSeeds();
        List<User> savedUsers = new ArrayList<>(userSeeds.size());

        for (UserSeed userSeed : userSeeds) {
            User savedUser = userRepository.findByEmailIgnoreCase(userSeed.email())
                    .orElseGet(() -> userRepository.save(buildUser(userSeed)));
            savedUsers.add(savedUser);
        }

        for (int index = 0; index < ideaSeeds.size(); index++) {
            IdeaSeed ideaSeed = ideaSeeds.get(index);
            User author = savedUsers.get(index % savedUsers.size());
            if (!ideaRepository.existsByAuthorAndTitleIgnoreCase(author, ideaSeed.title())) {
                ideaRepository.save(buildIdea(ideaSeed, author));
            }
        }
        log.info("IdeaHub sample data seeding completed for database users={} ideas={}", userRepository.count(), ideaRepository.count());
    }

    private List<UserSeed> buildUserSeeds() {
        List<UserSeed> seeds = new ArrayList<>(10);
        for (int index = 1; index <= 10; index++) {
            String username = "23eg106e%02d".formatted(index);
            String email = username + "@anurag.edu.in";
            seeds.add(new UserSeed(username, email));
        }
        return seeds;
    }

    private List<IdeaSeed> buildIdeaSeeds() {
        return List.of(
                new IdeaSeed(
                        "AI Study Planner",
                        "An AI-powered study planning platform that builds weekly schedules, adapts to exam timelines, and helps students balance attendance, assignments, and revision."
                ),
                new IdeaSeed(
                        "Campus Ride Sharing App",
                        "A secure ride sharing app for college students that matches daily commuters, reduces travel costs, and verifies riders using institutional email accounts."
                ),
                new IdeaSeed(
                        "AI Resume Analyzer",
                        "A resume analysis service that reviews student resumes, highlights missing skills, suggests role-specific improvements, and benchmarks applications for internships."
                ),
                new IdeaSeed(
                        "Student Skill Marketplace",
                        "A marketplace where students can offer tutoring, design, coding, and event services while peers discover trusted campus talent through verified profiles."
                ),
                new IdeaSeed(
                        "AI Startup Idea Validator",
                        "A validation tool that scores startup ideas, identifies target users, suggests differentiation, and summarizes market risks for student founders."
                ),
                new IdeaSeed(
                        "Smart Hostel Management System",
                        "A hostel management system for complaints, attendance, visitor tracking, and room maintenance that improves operations for wardens and residents."
                ),
                new IdeaSeed(
                        "AI Exam Preparation Assistant",
                        "An exam preparation assistant that generates topic summaries, personalized quizzes, and revision checklists based on syllabus coverage and confidence gaps."
                ),
                new IdeaSeed(
                        "College Event Discovery Platform",
                        "A platform that aggregates college events, workshops, hackathons, and club activities so students can discover opportunities in one curated feed."
                ),
                new IdeaSeed(
                        "AI Code Debugger for Students",
                        "A debugging assistant that explains compiler errors in simple language, suggests likely fixes, and teaches students why the issue occurred."
                ),
                new IdeaSeed(
                        "Peer-to-Peer Book Exchange",
                        "A book exchange network where students can lend, request, and swap academic books with transparent availability, location, and trust indicators."
                )
        );
    }

    private User buildUser(UserSeed userSeed) {
        User user = new User();
        user.setUsername(userSeed.username());
        user.setEmail(userSeed.email());
        user.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole(Role.ROLE_USER);
        user.setBio("Student builder exploring practical startup ideas for campus and career growth.");
        user.setAvatarUrl(null);
        return user;
    }

    private Idea buildIdea(IdeaSeed ideaSeed, User author) {
        Idea idea = new Idea();
        idea.setTitle(ideaSeed.title());
        idea.setDescription(ideaSeed.description());
        idea.setVisibility(IdeaVisibility.PUBLIC);
        idea.setAuthor(author);
        return idea;
    }

    private record UserSeed(String username, String email) {
    }

    private record IdeaSeed(String title, String description) {
    }
}
