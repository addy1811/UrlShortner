package com.shortner.repository;

import com.shortner.entity.FormResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FormResponseRepository extends JpaRepository<FormResponse, UUID> {

    // Paginated since a popular link could accumulate a lot of submissions -
    // owner's response dashboard shouldn't load them all at once.
    Page<FormResponse> findByLinkIdOrderBySubmittedAtDesc(UUID linkId, Pageable pageable);

    long countByLinkId(UUID linkId);
}