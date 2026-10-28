package com.influencer.influencer_platform.controller;

import com.influencer.influencer_platform.dto.request.BidRequest;
import com.influencer.influencer_platform.dto.request.SubmissionRequest;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignResponse;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.entity.CampaignAssignment;
import com.influencer.influencer_platform.entity.ContentSubmission;
import com.influencer.influencer_platform.enums.AssignmentStatus;
import com.influencer.influencer_platform.enums.BidStatus;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.repository.CampaignAssignmentRepository;
import com.influencer.influencer_platform.repository.ContentSubmissionRepository;
import com.influencer.influencer_platform.repository.InfluencerProfileRepository;
import com.influencer.influencer_platform.security.UserPrincipal;
import com.influencer.influencer_platform.service.BidService;
import com.influencer.influencer_platform.service.CampaignService;
import com.influencer.influencer_platform.service.ProfileService;
import com.influencer.influencer_platform.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/influencer")
@PreAuthorize("hasRole('INFLUENCER')")
@RequiredArgsConstructor
public class InfluencerController {

    private final ProfileService profileService;
    private final CampaignService campaignService;
    private final BidService bidService;
    private final SubmissionService submissionService;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final CampaignAssignmentRepository assignmentRepository;
    private final ContentSubmissionRepository submissionRepository;

    @GetMapping("/profile")
    public ResponseEntity<InfluencerProfileDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getMyInfluencerProfile(authentication));
    }

    @PutMapping("/profile")
    public ResponseEntity<InfluencerProfileDto> updateProfile(
            @Valid @RequestBody InfluencerProfileDto request, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateInfluencerProfile(request, authentication));
    }

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignResponse>> getCampaigns() {
        return ResponseEntity.ok(campaignService.getOpenCampaigns(null, null));
    }

    @GetMapping("/campaigns/{campaignId}")
    public ResponseEntity<CampaignResponse> getCampaign(@PathVariable Long campaignId) {
        return ResponseEntity.ok(campaignService.getCampaignById(campaignId));
    }

    @GetMapping("/bids")
    public ResponseEntity<List<BidResponse>> getBids(Authentication authentication) {
        return ResponseEntity.ok(bidService.getMyBids(authentication));
    }

    @PostMapping("/bids")
    public ResponseEntity<BidResponse> createBid(
            @Valid @RequestBody BidRequest request, Authentication authentication) {
        return ResponseEntity.status(201).body(bidService.createBid(request, authentication));
    }

    @GetMapping("/assignments")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getAssignments(Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        Long profileId = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"))
                .getId();
        List<Map<String, Object>> assignments = assignmentRepository.findByInfluencerProfileId(profileId).stream()
                .map(this::toAssignmentResponse)
                .toList();
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/assignments/{assignmentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAssignment(
            @PathVariable Long assignmentId, Authentication authentication) {
        CampaignAssignment assignment = getOwnedAssignment(assignmentId, authentication);
        return ResponseEntity.ok(toAssignmentResponse(assignment));
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<?> createSubmission(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionRequest request,
            Authentication authentication) {
        request.setAssignmentId(assignmentId);
        return ResponseEntity.status(201).body(submissionService.createSubmission(request, authentication));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats(Authentication authentication) {
        List<BidResponse> bids = bidService.getMyBids(authentication);
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        Long profileId = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"))
                .getId();
        long activeAssignments = assignmentRepository.findByInfluencerProfileId(profileId).stream()
                .filter(a -> a.getStatus() != AssignmentStatus.COMPLETED)
                .count();
        long activeBids = bids.stream().filter(b -> b.getStatus() == BidStatus.PENDING).count();
        return ResponseEntity.ok(Map.of("activeBids", activeBids, "activeAssignments", activeAssignments));
    }

    @GetMapping("/dashboard/activity")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        Long profileId = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"))
                .getId();
        List<Map<String, Object>> activity = new ArrayList<>();
        bidService.getMyBids(authentication).forEach(bid -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bid.getId());
            item.put("message", "Bid submitted for " + bid.getCampaign().getTitle());
            item.put("createdAt", bid.getSubmittedAt());
            activity.add(item);
        });
        assignmentRepository.findByInfluencerProfileId(profileId).forEach(assignment -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", assignment.getId());
            item.put("message", "Assigned to " + assignment.getCampaign().getTitle());
            item.put("createdAt", assignment.getAssignedAt());
            activity.add(item);
        });
        activity.sort(Comparator.comparing(
                item -> (LocalDateTime) item.get("createdAt"), Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(activity.stream().limit(20).toList());
    }

    private CampaignAssignment getOwnedAssignment(Long assignmentId, Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        CampaignAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));
        if (!assignment.getInfluencerProfile().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Assignment not found with id: " + assignmentId);
        }
        return assignment;
    }

    private Map<String, Object> toAssignmentResponse(CampaignAssignment assignment) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", assignment.getId());
        response.put("campaignTitle", assignment.getCampaign().getTitle());
        response.put("category", assignment.getCampaign().getCategory());
        response.put("brand", assignment.getCampaign().getBrandProfile().getCompanyName());
        response.put("acceptedAmount", assignment.getBid().getProposedAmount());
        response.put("deadline", assignment.getCampaign().getDeadline());
        response.put("status", assignment.getStatus() == AssignmentStatus.COMPLETED ? "Completed" : "Active");
        String description = assignment.getCampaign().getDescription();
        response.put("objectives", description == null || description.isBlank() ? List.of() : List.of(description));
        ContentSubmission submission = submissionRepository.findByAssignmentId(assignment.getId()).orElse(null);
        response.put("submissions", submission == null ? List.of() : List.of(Map.of(
                "id", submission.getId(),
                "title", submission.getDescription() == null ? "Content submission" : submission.getDescription(),
                "type", "Content",
                "submittedDate", submission.getSubmittedAt(),
                "status", switch (submission.getStatus()) {
                    case ACCEPTED -> "Approved";
                    case REJECTED -> "Rejected";
                    default -> "Pending";
                }
        )));
        return response;
    }
}
