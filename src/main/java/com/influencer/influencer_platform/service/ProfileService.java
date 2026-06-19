package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.dto.response.ProfileResponse;
import com.influencer.influencer_platform.dto.response.SocialMediaLinkDto;
import com.influencer.influencer_platform.entity.*;
import com.influencer.influencer_platform.enums.UserRole;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final SocialMediaLinkRepository socialMediaLinkRepository;

    public ProfileResponse getMyProfile(Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToProfileResponse(user);
    }

    public BrandProfileDto getBrandProfile(Long userId, Authentication authentication) {
        User currentUser = userRepository.findById(
                ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() != UserRole.BRAND && !currentUser.getId().equals(targetUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this profile");
        }

        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        return mapToBrandProfileDto(brandProfile);
    }

    public InfluencerProfileDto getInfluencerProfile(Long userId, Authentication authentication) {
        User currentUser = userRepository.findById(
                ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId()
        ).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        if (currentUser.getRole() != UserRole.BRAND && !currentUser.getId().equals(targetUser.getId())) {
            throw new UnauthorizedException("You are not authorized to view this profile");
        }

        return mapToInfluencerProfileDto(influencerProfile);
    }

    @Transactional
    public BrandProfileDto updateBrandProfile(BrandProfileDto request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        BrandProfile brandProfile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand profile not found"));

        brandProfile.setCompanyName(request.getCompanyName());
        brandProfile.setIndustry(request.getIndustry());
        brandProfile.setWebsite(request.getWebsite());
        brandProfile.setBio(request.getBio());
        brandProfile.setLogoUrl(request.getLogoUrl());

        brandProfile = brandProfileRepository.save(brandProfile);

        return mapToBrandProfileDto(brandProfile);
    }

    @Transactional
    public InfluencerProfileDto updateInfluencerProfile(InfluencerProfileDto request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        influencerProfile.setNiche(request.getNiche());
        influencerProfile.setLocation(request.getLocation());
        influencerProfile.setFollowerCount(request.getFollowerCount());
        influencerProfile.setEngagementRate(request.getEngagementRate());
        influencerProfile.setProfilePicUrl(request.getProfilePicUrl());
        influencerProfile.setBio(request.getBio());

        influencerProfile = influencerProfileRepository.save(influencerProfile);

        return mapToInfluencerProfileDto(influencerProfile);
    }

    @Transactional
    public SocialMediaLinkDto addSocialMediaLink(SocialMediaLinkDto request, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        SocialMediaLink link = SocialMediaLink.builder()
                .influencerProfile(influencerProfile)
                .platform(request.getPlatform())
                .url(request.getUrl())
                .followerCount(request.getFollowerCount())
                .build();

        link = socialMediaLinkRepository.save(link);

        return mapToSocialMediaLinkDto(link);
    }

    @Transactional
    public void deleteSocialMediaLink(Long linkId, Authentication authentication) {
        Long userId = ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        SocialMediaLink link = socialMediaLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Social media link not found"));

        if (!link.getInfluencerProfile().getId().equals(influencerProfile.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this link");
        }

        socialMediaLinkRepository.delete(link);
    }

    private ProfileResponse mapToProfileResponse(User user) {
        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole().name());

        if (user.getRole() == UserRole.BRAND) {
            BrandProfile brandProfile = brandProfileRepository.findByUserId(user.getId()).orElse(null);
            if (brandProfile != null) {
                builder.brandProfile(mapToBrandProfileDto(brandProfile));
            }
        } else {
            InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(user.getId()).orElse(null);
            if (influencerProfile != null) {
                builder.influencerProfile(mapToInfluencerProfileDto(influencerProfile));
            }
        }

        return builder.build();
    }

    private BrandProfileDto mapToBrandProfileDto(BrandProfile profile) {
        return BrandProfileDto.builder()
                .id(profile.getId())
                .companyName(profile.getCompanyName())
                .industry(profile.getIndustry())
                .website(profile.getWebsite())
                .bio(profile.getBio())
                .logoUrl(profile.getLogoUrl())
                .build();
    }

    private InfluencerProfileDto mapToInfluencerProfileDto(InfluencerProfile profile) {
        List<SocialMediaLinkDto> links = profile.getSocialMediaLinks().stream()
                .map(this::mapToSocialMediaLinkDto)
                .collect(Collectors.toList());

        return InfluencerProfileDto.builder()
                .id(profile.getId())
                .niche(profile.getNiche())
                .location(profile.getLocation())
                .followerCount(profile.getFollowerCount())
                .engagementRate(profile.getEngagementRate())
                .profilePicUrl(profile.getProfilePicUrl())
                .bio(profile.getBio())
                .socialMediaLinks(links)
                .build();
    }

    private SocialMediaLinkDto mapToSocialMediaLinkDto(SocialMediaLink link) {
        return SocialMediaLinkDto.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .followerCount(link.getFollowerCount())
                .build();
    }
}
