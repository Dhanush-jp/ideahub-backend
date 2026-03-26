package com.ideahub.backend.service;

import com.ideahub.backend.ai.TextToSQLService;
import com.ideahub.backend.dto.admin.AdminQueryResponse;
import com.ideahub.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminQueryService {

    private static final List<String> DANGEROUS_KEYWORDS = List.of(
            "drop",
            "delete",
            "truncate",
            "alter",
            "insert",
            "update",
            "create",
            "grant",
            "revoke",
            "exec",
            "execute",
            "call"
    );
    private static final Set<String> ALLOWED_TABLES = Set.of("users", "ideas", "comments", "reactions", "follows", "saved_ideas");
    private static final Pattern TABLE_PATTERN = Pattern.compile("\\b(from|join)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");

    private final TextToSQLService textToSQLService;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public AdminQueryResponse runNaturalLanguageQuery(String question) {
        String sql = textToSQLService.generateSelectSql(question);
        validateSql(sql);

        String limitedSql = sql.toLowerCase(Locale.ROOT).contains(" limit ") ? sql : sql + " LIMIT 200";
        log.info("Admin SQL generated. question='{}' sql='{}'", question, limitedSql);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(limitedSql);

        return AdminQueryResponse.builder()
                .generatedSql(limitedSql)
                .data(rows)
                .build();
    }

    private void validateSql(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new BadRequestException("Generated SQL is empty");
        }

        String normalized = sql.trim().toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("select")) {
            throw new BadRequestException("Only SELECT queries are allowed");
        }

        if (normalized.contains("--") || normalized.contains("/*")) {
            throw new BadRequestException("Unsafe SQL blocked: comments are not allowed");
        }

        if (normalized.contains(";")) {
            throw new BadRequestException("Multiple statements are not allowed");
        }

        for (String keyword : DANGEROUS_KEYWORDS) {
            if (normalized.matches(".*\\b" + keyword + "\\b.*")) {
                throw new BadRequestException("Unsafe SQL blocked: " + keyword.toUpperCase(Locale.ROOT));
            }
        }

        Matcher matcher = TABLE_PATTERN.matcher(normalized);
        while (matcher.find()) {
            String table = matcher.group(2).toLowerCase(Locale.ROOT);
            if (!ALLOWED_TABLES.contains(table)) {
                throw new BadRequestException("Unsafe SQL blocked: table '" + table + "' is not allowed");
            }
        }
    }
}
