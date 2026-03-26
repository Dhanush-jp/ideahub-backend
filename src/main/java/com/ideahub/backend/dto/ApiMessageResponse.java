package com.ideahub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiMessageResponse {
    private final String message;
}
