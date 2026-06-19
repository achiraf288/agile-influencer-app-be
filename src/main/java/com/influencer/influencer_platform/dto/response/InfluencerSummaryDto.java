package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfluencerSummaryDto {
    private Long id;
    private String fullName;
    private String niche;
    private Long followerCount;
    private Double engagementRate;
    private String profilePicUrl;
}
