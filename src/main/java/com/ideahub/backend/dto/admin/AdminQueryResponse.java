package com.ideahub.backend.dto.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AdminQueryResponse {

    @JsonProperty("generated_sql")
    private final String generatedSql;

    private final List<Map<String, Object>> data;
}