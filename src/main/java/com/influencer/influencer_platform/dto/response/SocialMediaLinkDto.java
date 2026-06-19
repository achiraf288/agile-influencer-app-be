package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialMediaLinkDto {
    private Long id;
    private String platform;
    private String url;
    private Long followerCount;
}
