package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.ReviewRequest;
import com.influencer.influencer_platform.dto.request.SubmissionRequest;
import com.influencer.influencer_platform.dto.response.SubmissionResponse;
import com.influencer.influencer_platform.entity.*;
import com.influencer.influencer_platform.enums.*;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.CampaignAssignmentRepository;
import com.influencer.influencer_platform.repository.ContentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final ContentSubmissionRepository contentSubmissionRepository;
    private final CampaignAssignmentRepository campaignAssignmentRepository;
    private final NotificationService notificationService;

    @Transactional
    public SubmissionResponse createSubmission(SubmissionRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        CampaignAssignment assignment = campaignAssignmentRepository.findById(request.getAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + request.getAssignmentId()));

        if (!assignment.getInfluencerProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to submit content for this assignment");
        }

        if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
            throw new UnauthorizedException("Assignment is not in a valid state for submission");
        }

        ContentSubmission submission = ContentSubmission.builder()
                .assignment(assignment)
                .contentUrl(request.getContentUrl())
                .description(request.getDescription())
                .status(BidStatus.PENDING)
                .build();

        submission = contentSubmissionRepository.save(submission);

        assignment.setStatus(AssignmentStatus.CONTENT_SUBMITTED);
        campaignAssignmentRepository.save(assignment);

        notificationService.notifyContentSubmitted(
                assignment.getCampaign().getBrandProfile().getUser().getId(),
                assignment.getInfluencerProfile().getUser().getFullName(),
                assignment.getCampaign().getTitle()
        );

        return mapToResponse(submission);
    }

    public SubmissionResponse getSubmissionByAssignment(Long assignmentId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        CampaignAssignment assignment = campaignAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found with id: " + assignmentId));

        if (!assignment.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this submission");
        }

        ContentSubmission submission = contentSubmissionRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        return mapToResponse(submission);
    }

    @Transactional
    public SubmissionResponse reviewSubmission(Long submissionId, ReviewRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        ContentSubmission submission = contentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id: " + submissionId));

        if (!submission.getAssignment().getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to review this submission");
        }

        BidStatus status = BidStatus.valueOf(request.getStatus().toUpperCase());
        submission.setStatus(status);
        submission.setBrandFeedback(request.getFeedback());
        submission = contentSubmissionRepository.save(submission);

        CampaignAssignment assignment = submission.getAssignment();
        if (status == BidStatus.ACCEPTED) {
            assignment.setStatus(AssignmentStatus.COMPLETED);
        } else {
            assignment.setStatus(AssignmentStatus.REVISION_REQUESTED);
        }
        campaignAssignmentRepository.save(assignment);

        notificationService.notifyContentReviewed(
                assignment.getInfluencerProfile().getUser().getId(),
                status.name(),
                assignment.getCampaign().getTitle()
        );

        return mapToResponse(submission);
    }

    private SubmissionResponse mapToResponse(ContentSubmission submission) {
        return SubmissionResponse.builder()
                .id(submission.getId())
                .contentUrl(submission.getContentUrl())
                .description(submission.getDescription())
                .status(submission.getStatus())
                .submittedAt(submission.getSubmittedAt())
                .brandFeedback(submission.getBrandFeedback())
                .build();
    }
}
