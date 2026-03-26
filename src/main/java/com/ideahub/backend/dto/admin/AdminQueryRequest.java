package com.ideahub.backend.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminQueryRequest {
    // Frontend historically used `question`; support both for compatibility.
    private String question;
    private String query;
}
