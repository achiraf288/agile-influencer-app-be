package com.influencer.influencer_platform.dto.response;

import com.influencer.influencer_platform.enums.CampaignStatus;
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
public class CampaignResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private BigDecimal budget;
    private LocalDateTime deadline;
    private CampaignStatus status;
    private LocalDateTime createdAt;
    private Long brandProfileId;
    private String companyName;
}
