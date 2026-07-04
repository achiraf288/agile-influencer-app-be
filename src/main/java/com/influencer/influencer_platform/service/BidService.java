package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.BidRequest;
import com.influencer.influencer_platform.dto.response.BidResponse;
import com.influencer.influencer_platform.dto.response.CampaignSummaryDto;
import com.influencer.influencer_platform.dto.response.InfluencerSummaryDto;
import com.influencer.influencer_platform.entity.*;
import com.influencer.influencer_platform.enums.*;
import com.influencer.influencer_platform.exception.DuplicateBidException;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignAssignmentRepository campaignAssignmentRepository;
    private final NotificationService notificationService;

    @Transactional
    public BidResponse createBid(BidRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + request.getCampaignId()));

        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new UnauthorizedException("Campaign is not open for bidding");
        }

        if (bidRepository.findByCampaignIdAndInfluencerId(request.getCampaignId(), userId).isPresent()) {
            throw new DuplicateBidException("You have already bid on this campaign");
        }

        Bid bid = Bid.builder()
                .campaign(campaign)
                .influencer(user)
                .message(request.getMessage())
                .proposedBudget(request.getProposedBudget())
                .status(BidStatus.PENDING)
                .build();

        bid = bidRepository.save(bid);

        notificationService.notifyBidReceived(
                campaign.getBrandProfile().getUser().getId(),
                user.getFullName(),
                campaign.getTitle()
        );

        return mapToResponse(bid);
    }

    public List<BidResponse> getBidsByCampaign(Long campaignId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with id: " + campaignId));

        if (!campaign.getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view bids for this campaign");
        }

        return bidRepository.findByCampaignId(campaignId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BidResponse> getMyBids(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();

        return bidRepository.findByInfluencerId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public BidResponse acceptBid(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to accept this bid");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Bid is not in pending status");
        }

        bid.setStatus(BidStatus.ACCEPTED);
        bid = bidRepository.save(bid);

        Campaign campaign = bid.getCampaign();
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaignRepository.save(campaign);

        CampaignAssignment assignment = CampaignAssignment.builder()
                .bid(bid)
                .campaign(campaign)
                .influencerProfile(null) // TODO: Update when influencer profile relationship is clarified
                .status(AssignmentStatus.ASSIGNED)
                .build();
        campaignAssignmentRepository.save(assignment);

        notificationService.notifyBidAccepted(
                bid.getInfluencer().getId(),
                campaign.getTitle()
        );

        return mapToResponse(bid);
    }

    @Transactional
    public BidResponse rejectBid(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to reject this bid");
        }

        bid.setStatus(BidStatus.REJECTED);
        bid = bidRepository.save(bid);

        notificationService.notifyBidRejected(
                bid.getInfluencer().getId(),
                bid.getCampaign().getTitle()
        );

        return mapToResponse(bid);
    }

    public BidResponse getBidById(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getInfluencer().getId().equals(userId) && 
            !bid.getCampaign().getBrandProfile().getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this bid");
        }

        return mapToResponse(bid);
    }

    @Transactional
    public BidResponse updateBid(Long bidId, BidRequest request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getInfluencer().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this bid");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Only pending bids can be updated");
        }

        bid.setMessage(request.getMessage());
        bid.setProposedBudget(request.getProposedBudget());
        bid = bidRepository.save(bid);

        return mapToResponse(bid);
    }

    @Transactional
    public void deleteBid(Long bidId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new ResourceNotFoundException("Bid not found with id: " + bidId));

        if (!bid.getInfluencer().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to delete this bid");
        }

        if (bid.getStatus() != BidStatus.PENDING) {
            throw new UnauthorizedException("Only pending bids can be deleted");
        }

        bidRepository.delete(bid);
    }

    private BidResponse mapToResponse(Bid bid) {
        InfluencerSummaryDto influencer = InfluencerSummaryDto.builder()
                .id(bid.getInfluencer().getId())
                .fullName(bid.getInfluencer().getFullName())
                .niche(null) // TODO: Get from influencer profile
                .followerCount(null) // TODO: Get from influencer profile
                .engagementRate(null) // TODO: Get from influencer profile
                .profilePicUrl(null) // TODO: Get from influencer profile
                .build();

        CampaignSummaryDto campaign = CampaignSummaryDto.builder()
                .id(bid.getCampaign().getId())
                .title(bid.getCampaign().getTitle())
                .category(bid.getCampaign().getCategory())
                .location(bid.getCampaign().getLocation())
                .build();

        return BidResponse.builder()
                .id(bid.getId())
                .message(bid.getMessage())
                .proposedBudget(bid.getProposedBudget())
                .status(bid.getStatus())
                .createdAt(bid.getCreatedAt())
                .influencer(influencer)
                .campaign(campaign)
                .build();
    }
}
