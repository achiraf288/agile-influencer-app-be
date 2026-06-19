package com.influencer.influencer_platform.repository;

import com.influencer.influencer_platform.entity.ContentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentSubmissionRepository extends JpaRepository<ContentSubmission, Long> {
    Optional<ContentSubmission> findByAssignmentId(Long assignmentId);
}
