package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDashboardDto {
    private Long totalCampaigns;
    private Long activeCampaigns;
    private Long pendingBids;
    private Long acceptedBids;
    private Double totalBudget;
    private Long totalInfluencers;
    private List<RecentActivityDto> recentActivity;
}
