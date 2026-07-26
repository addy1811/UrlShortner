package com.shortner.repository;

import com.shortner.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    Page<AccessLog> findByLinkIdOrderByAccessedAtDesc(UUID linkId, Pageable pageable);

    // Surfaces denied attempts specifically - backed by the partial index from V6,
    // so this stays fast even as access_logs grows large.
    Page<AccessLog> findByLinkIdAndAccessGrantedFalseOrderByAccessedAtDesc(UUID linkId, Pageable pageable);

    long countByLinkIdAndAccessedAtAfter(UUID linkId, Instant since);
}