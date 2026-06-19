package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.request.ReviewRequest;
import com.influencer.influencer_platform.dto.request.SubmissionRequest;
import com.influencer.influencer_platform.dto.response.SubmissionResponse;
import com.influencer.influencer_platform.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    @PreAuthorize("hasRole('INFLUENCER')")
    public ResponseEntity<SubmissionResponse> createSubmission(@Valid @RequestBody SubmissionRequest request, Authentication authentication) {
        SubmissionResponse response = submissionService.createSubmission(request, authentication);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<SubmissionResponse> getSubmissionByAssignment(@PathVariable Long assignmentId, Authentication authentication) {
        SubmissionResponse response = submissionService.getSubmissionByAssignment(assignmentId, authentication);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('BRAND')")
    public ResponseEntity<SubmissionResponse> reviewSubmission(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        SubmissionResponse response = submissionService.reviewSubmission(id, request, authentication);
        return ResponseEntity.ok(response);
    }
}
