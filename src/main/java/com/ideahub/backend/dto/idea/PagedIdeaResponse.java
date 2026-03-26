package com.ideahub.backend.dto.idea;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PagedIdeaResponse {
    private final List<IdeaResponse> items;
    private final int page;
    private final int size;
    private final long totalItems;
    private final int totalPages;
}
