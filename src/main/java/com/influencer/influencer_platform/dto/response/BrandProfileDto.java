package com.influencer.influencer_platform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandProfileDto {
    private Long id;
    private String companyName;
    private String industry;
    private String website;
    private String bio;
    private String logoUrl;
}
