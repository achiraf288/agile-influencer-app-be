package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.Campaign;
import com.influencer.influencer_platform.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long>, JpaSpecificationExecutor<Campaign> {
    List<Campaign> findByStatus(CampaignStatus status);
    List<Campaign> findByBrandProfileId(Long brandProfileId);
    
    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.brandProfile bp LEFT JOIN FETCH bp.user WHERE c.id = :id")
    Optional<Campaign> findByIdWithBrandProfile(@Param("id") Long id);
    
    @Query("SELECT c FROM Campaign c LEFT JOIN FETCH c.brandProfile bp LEFT JOIN FETCH bp.user WHERE c.brandProfile.id = :brandProfileId")
    List<Campaign> findByBrandProfileIdWithBrandProfile(@Param("brandProfileId") Long brandProfileId);
}
