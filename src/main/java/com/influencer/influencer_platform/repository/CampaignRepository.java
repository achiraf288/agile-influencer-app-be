package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.Campaign;
import com.influencer.influencer_platform.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByBrandProfileId(Long brandProfileId);
}
