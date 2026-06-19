package com.influencer.influencer_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidRequest {
    
    @NotNull(message = "Campaign ID is required")
    private Long campaignId;
    
    @NotBlank(message = "Proposal is required")
    @Size(min = 50, message = "Proposal must be at least 50 characters")
    private String proposal;
    
    @NotNull(message = "Proposed amount is required")
    private BigDecimal proposedAmount;
}
