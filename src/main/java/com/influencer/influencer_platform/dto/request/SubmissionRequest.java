package com.influencer.influencer_platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {
    
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
    
    @NotBlank(message = "Content URL is required")
    @Pattern(regexp = "^(http|https)://.*", message = "Content URL must be a valid URL")
    private String contentUrl;
    
    private String description;
}
