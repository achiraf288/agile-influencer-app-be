package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.CampaignAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignAssignmentRepository extends JpaRepository<CampaignAssignment, Long> {
    Optional<CampaignAssignment> findByBidId(Long bidId);
    List<CampaignAssignment> findByInfluencerProfileId(Long influencerProfileId);
}
