package com.influencer.influencer_platform.dto.response;

import com.influencer.influencer_platform.enums.BidStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponse {
    private Long id;
    private String proposal;
    private BigDecimal proposedAmount;
    private BidStatus status;
    private LocalDateTime submittedAt;
    private InfluencerSummaryDto influencer;
    private CampaignSummaryDto campaign;
}
