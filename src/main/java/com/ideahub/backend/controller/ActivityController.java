package com.ideahub.backend.controller;

import com.ideahub.backend.dto.ApiResponse;
import com.ideahub.backend.dto.activity.ActivityResponse;
import com.ideahub.backend.service.ActivityService;
import com.ideahub.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    @GetMapping
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity() {
        return ResponseEntity.ok(ApiResponse.success(activityService.getActivity(SecurityUtils.getCurrentUserId())));
    }
}
