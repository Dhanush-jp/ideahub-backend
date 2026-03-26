package com.ideahub.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class ErrorResponse {
    private final Instant timestamp;
    private final String status;
    private final int code;
    private final String message;
    private final String path;
    private final List<String> details;
}
