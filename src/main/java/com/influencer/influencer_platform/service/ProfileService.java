package com.influencer.influencer_platform.service;

import com.influencer.influencer_platform.dto.request.RegisterInfluencerStep3Request;
import com.influencer.influencer_platform.dto.response.BrandProfileDto;
import com.influencer.influencer_platform.dto.response.InfluencerProfileDto;
import com.influencer.influencer_platform.dto.response.ProfileResponse;
import com.influencer.influencer_platform.dto.response.SocialMediaLinkDto;
import com.influencer.influencer_platform.entity.*;
import com.influencer.influencer_platform.enums.UserRole;
import com.influencer.influencer_platform.exception.ResourceNotFoundException;
import com.influencer.influencer_platform.exception.UnauthorizedException;
import com.influencer.influencer_platform.repository.*;
import com.influencer.influencer_platform.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final BrandProfileRepository brandProfileRepository;
    private final InfluencerProfileRepository influencerProfileRepository;
    private final SocialMediaLinkRepository socialMediaLinkRepository;
    private final FileStorageService fileStorageService;

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
        Long userId = getAuthenticatedUserId(authentication);
        
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
        Long userId = getAuthenticatedUserId(authentication);
        
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
        Long userId = getAuthenticatedUserId(authentication);
        
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
        Long userId = getAuthenticatedUserId(authentication);
        
        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        SocialMediaLink link = socialMediaLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("Social media link not found"));

        if (!link.getInfluencerProfile().getId().equals(influencerProfile.getId())) {
            throw new UnauthorizedException("You are not authorized to delete this link");
        }

        socialMediaLinkRepository.delete(link);
    }

    @Transactional
    public InfluencerProfileDto completeInfluencerStep1(String username, String niche, String bio, MultipartFile profilePic, Long userId, Authentication authentication) {
        Long resolvedUserId = resolveUserId(userId, authentication);

        User user = userRepository.findById(resolvedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.INFLUENCER) {
            throw new UnauthorizedException("Only influencer accounts can complete influencer onboarding");
        }

        if (username != null && !username.isBlank()) {
            user.setFullName(username.trim());
            userRepository.save(user);
        }

        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(resolvedUserId)
                .orElseGet(() -> InfluencerProfile.builder().user(user).niche(niche).build());

        influencerProfile.setNiche(niche);
        influencerProfile.setBio(bio);

        if (profilePic != null && !profilePic.isEmpty()) {
            influencerProfile.setProfilePicUrl(fileStorageService.store(profilePic, "influencers", resolvedUserId));
        }

        influencerProfile = influencerProfileRepository.save(influencerProfile);
        return mapToInfluencerProfileDto(influencerProfile);
    }

    @Transactional
    public InfluencerProfileDto completeInfluencerStep3(RegisterInfluencerStep3Request request, Authentication authentication) {
        Long userId = resolveUserId(request.getUserId(), authentication);

        if (request.getUserId() != null && !Objects.equals(request.getUserId(), userId)) {
            throw new UnauthorizedException("You are not authorized to update this profile");
        }

        InfluencerProfile influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));

        socialMediaLinkRepository.deleteAll(influencerProfile.getSocialMediaLinks());
        influencerProfile.getSocialMediaLinks().clear();

        if (request.getPlatforms() != null) {
            for (RegisterInfluencerStep3Request.SocialMediaPlatformRequest platformRequest : request.getPlatforms()) {
                if (platformRequest == null || platformRequest.getPlatform() == null || platformRequest.getUrl() == null) {
                    continue;
                }

                SocialMediaLink link = SocialMediaLink.builder()
                        .influencerProfile(influencerProfile)
                        .platform(platformRequest.getPlatform())
                        .url(platformRequest.getUrl())
                        .followerCount(platformRequest.getFollowerCount())
                        .build();
                socialMediaLinkRepository.save(link);
            }
        }

        influencerProfile = influencerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Influencer profile not found"));
        return mapToInfluencerProfileDto(influencerProfile);
    }

    @Transactional
    public BrandProfileDto completeBrandStep2(String companyName, String industry, String website, String bio, MultipartFile logo, Long userId, Authentication authentication) {
        Long resolvedUserId = resolveUserId(userId, authentication);

        User user = userRepository.findById(resolvedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != UserRole.BRAND) {
            throw new UnauthorizedException("Only brand accounts can complete brand onboarding");
        }

        BrandProfile brandProfile = brandProfileRepository.findByUserId(resolvedUserId)
                .orElseGet(() -> BrandProfile.builder().user(user).companyName(companyName).build());

        brandProfile.setCompanyName(companyName);
        brandProfile.setIndustry(industry);
        brandProfile.setWebsite(website);
        brandProfile.setBio(bio);

        if (logo != null && !logo.isEmpty()) {
            brandProfile.setLogoUrl(fileStorageService.store(logo, "brands", resolvedUserId));
        }

        brandProfile = brandProfileRepository.save(brandProfile);
        return mapToBrandProfileDto(brandProfile);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        return ((com.influencer.influencer_platform.security.UserPrincipal) authentication.getPrincipal()).getId();
    }

    private Long resolveUserId(Long requestedUserId, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof com.influencer.influencer_platform.security.UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }

        if (requestedUserId != null) {
            return requestedUserId;
        }

        throw new UnauthorizedException("Authentication required for onboarding");
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
