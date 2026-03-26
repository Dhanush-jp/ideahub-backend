package com.ideahub.backend.ai;

import com.ideahub.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextToSQLService {

    private final HuggingFaceClient huggingFaceClient;

    @Value("${app.ai.huggingface.text-to-sql-model:google/flan-t5-large}")
    private String textToSqlModel;

    @Value("${app.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${app.ai.mock-on-failure:true}")
    private boolean mockOnFailure;

    public String generateSelectSql(String question) {
        if (!aiEnabled) {
            return fallbackSql(question);
        }

        String prompt = """
                Convert natural language to a single PostgreSQL SELECT query.
                Return only SQL with no markdown.
                Database schema:
                users(id, username, email, created_at)
                ideas(id, title, description, visibility, author_id, ai_score, created_at)
                comments(id, content, idea_id, author_id, created_at)
                reactions(id, idea_id, user_id, type, created_at)
                follows(id, follower_id, following_id, created_at)
                saved_ideas(id, user_id, idea_id, created_at)
                
                Important:
                - "likes" means reactions rows where type = 'LIKE'
                - Only query the tables listed above
                - Prefer LIMIT 200 for large result sets
                Question: %s
                """.formatted(question);

        try {
            String raw = huggingFaceClient.generateText(textToSqlModel, prompt);
            String sql = sanitizeSql(raw);

            if (!sql.toLowerCase(Locale.ROOT).startsWith("select")) {
                throw new BadRequestException("Generated SQL is not a SELECT query");
            }

            return sql;
        } catch (BadRequestException ex) {
            if (!mockOnFailure) {
                throw ex;
            }
            log.warn("Falling back to local text-to-sql generation: {}", ex.getMessage());
            return fallbackSql(question);
        }
    }

    private String sanitizeSql(String raw) {
        String normalized = raw.replace("```sql", "").replace("```", "").trim();
        int selectIndex = normalized.toLowerCase(Locale.ROOT).indexOf("select");
        if (selectIndex < 0) {
            return normalized;
        }
        String sql = normalized.substring(selectIndex).trim();
        int semicolonIndex = sql.indexOf(';');
        if (semicolonIndex >= 0) {
            sql = sql.substring(0, semicolonIndex).trim();
        }
        return sql;
    }

    private String fallbackSql(String question) {
        String normalized = question == null ? "" : question.toLowerCase(Locale.ROOT);

        if (normalized.contains("user")) {
            return "SELECT id, username, email, role, created_at FROM users ORDER BY created_at DESC LIMIT 200";
        }
        if (normalized.contains("comment")) {
            return "SELECT id, content, idea_id, author_id, created_at FROM comments ORDER BY created_at DESC LIMIT 200";
        }
        if (normalized.contains("reaction") || normalized.contains("like")) {
            return "SELECT id, idea_id, user_id, type, created_at FROM reactions ORDER BY created_at DESC LIMIT 200";
        }
        return "SELECT id, title, visibility, author_id, created_at, ai_score FROM ideas ORDER BY created_at DESC LIMIT 200";
    }
}
