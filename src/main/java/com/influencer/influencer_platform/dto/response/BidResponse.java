package com.influencer.influencer_platform.dto.response;

import com.influencer.influencer_platform.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long id;
    private String message;
    private Double proposedBudget;
    private BidStatus status;
    private LocalDateTime createdAt;
    private InfluencerSummaryDto influencer;
    private CampaignSummaryDto campaign;
}
