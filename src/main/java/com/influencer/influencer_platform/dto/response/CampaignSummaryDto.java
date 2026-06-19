package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSummaryDto {
    private Long id;
    private String title;
    private String category;
    private String location;
}
