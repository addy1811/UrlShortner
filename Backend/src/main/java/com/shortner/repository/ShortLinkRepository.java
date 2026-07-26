package com.shortner.repository;

import com.shortner.entity.ShortLink;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShortLinkRepository extends JpaRepository<ShortLink, UUID> {

    // Redirect resolution can hit either the random short code or a vanity alias.
    Optional<ShortLink> findByShortCode(String shortCode);

    Optional<ShortLink> findByCustomAlias(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    Page<ShortLink> findByOwnerId(UUID ownerId, Pageable pageable);

    // Used by the service layer to enforce ownership in one query instead of
    // fetch-then-compare - returns empty if the link exists but belongs to someone else.
    Optional<ShortLink> findByIdAndOwnerId(UUID id, UUID ownerId);
}