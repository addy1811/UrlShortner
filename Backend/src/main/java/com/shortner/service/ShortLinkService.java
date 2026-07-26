package com.shortner.service;

import com.shortner.dto.link.CreateLinkRequest;
import com.shortner.dto.link.LinkResponse;
import com.shortner.dto.link.UpdateLinkRequest;
import com.shortner.entity.ShortLink;
import com.shortner.entity.User;
import com.shortner.entity.Visibility;
import com.shortner.exception.AccessDeniedException;
import com.shortner.exception.DuplicateAliasException;
import com.shortner.exception.LinkExpiredException;
import com.shortner.exception.LinkNotFoundException;
import com.shortner.repository.ShortLinkRepository;
import com.shortner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortLinkService {

    private final ShortLinkRepository shortLinkRepository;
    private final UserRepository userRepository;
    private final ShortCodeGeneratorService shortCodeGeneratorService;
    private final EncryptionService encryptionService;
    private final AccessControlService accessControlService;
    private final AccessLogService accessLogService;

    @Value("${app.short-link.base-redirect-url}")
    private String baseRedirectUrl;

    @Transactional
    public LinkResponse createLink(UUID ownerId, CreateLinkRequest request) {
        User owner = userRepository.getReferenceById(ownerId);

        if (request.customAlias() != null && shortLinkRepository.existsByCustomAlias(request.customAlias())) {
            throw new DuplicateAliasException("Custom alias '" + request.customAlias() + "' is already taken");
        }

        EncryptionService.EncryptedPayload encrypted = encryptionService.encrypt(request.destinationUrl());

        ShortLink link = ShortLink.builder()
            .shortCode(shortCodeGeneratorService.generateUniqueShortCode())
            .encryptedDestination(encrypted.ciphertext())
            .encryptionIv(encrypted.iv())
            .owner(owner)
            .visibility(request.visibility() != null ? request.visibility() : Visibility.PRIVATE)
            .customAlias(request.customAlias())
            .expiresAt(request.expiresAt())
            .maxUses(request.maxUses())
            .metadata(request.metadata() != null ? request.metadata() : new HashMap<>())
            .build();

        ShortLink saved = shortLinkRepository.save(link);
        return toResponse(saved, request.destinationUrl());
    }

    @Transactional(readOnly = true)
    public LinkResponse getLinkForOwner(UUID ownerId, UUID linkId) {
        ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));
        return toResponse(link, decrypt(link));
    }

    @Transactional(readOnly = true)
    public Page<LinkResponse> listLinksForOwner(UUID ownerId, Pageable pageable) {
        return shortLinkRepository.findByOwnerId(ownerId, pageable)
            .map(link -> toResponse(link, decrypt(link)));
    }

    @Transactional
    public LinkResponse updateLink(UUID ownerId, UUID linkId, UpdateLinkRequest request) {
        ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        if (request.destinationUrl() != null) {
            EncryptionService.EncryptedPayload encrypted = encryptionService.encrypt(request.destinationUrl());
            link.setEncryptedDestination(encrypted.ciphertext());
            link.setEncryptionIv(encrypted.iv());
        }
        if (request.visibility() != null) {
            link.setVisibility(request.visibility());
        }
        if (request.expiresAt() != null) {
            link.setExpiresAt(request.expiresAt());
        }
        if (request.maxUses() != null) {
            link.setMaxUses(request.maxUses());
        }
        if (request.active() != null) {
            link.setActive(request.active());
        }
        if (request.metadata() != null) {
            link.setMetadata(request.metadata());
        }

        ShortLink saved = shortLinkRepository.save(link);
        return toResponse(saved, decrypt(saved));
    }

    @Transactional
    public void deleteLink(UUID ownerId, UUID linkId) {
        ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));
        shortLinkRepository.delete(link);
    }

    /**
     * Resolves a short code/alias for redirect, enforcing visibility rules and
     * logging every attempt (granted or denied) via AccessLogService. This is the
     * one path anonymous, unauthenticated traffic ever touches.
     */
    @Transactional
    public String resolveForRedirect(String shortCodeOrAlias, UUID requestingUserId, String rawIpAddress) {
        ShortLink link = shortLinkRepository.findByShortCode(shortCodeOrAlias)
            .or(() -> shortLinkRepository.findByCustomAlias(shortCodeOrAlias))
            .orElseThrow(() -> new LinkNotFoundException("No link found for '" + shortCodeOrAlias + "'"));

        User requestingUser = requestingUserId != null ? userRepository.getReferenceById(requestingUserId) : null;

        try {
            accessControlService.assertAccessAllowed(link, requestingUserId);
        } catch (AccessDeniedException | LinkExpiredException | LinkNotFoundException e) {
            accessLogService.recordAttempt(link, requestingUser, rawIpAddress, false);
            throw e;
        }

        link.setUseCount(link.getUseCount() + 1);
        shortLinkRepository.save(link);
        accessLogService.recordAttempt(link, requestingUser, rawIpAddress, true);

        return decrypt(link);
    }

    private String decrypt(ShortLink link) {
        return encryptionService.decrypt(link.getEncryptedDestination(), link.getEncryptionIv());
    }

    private LinkResponse toResponse(ShortLink link, String destinationUrl) {
        String shortUrl = baseRedirectUrl + "/" + link.getShortCode();

        return new LinkResponse(
            link.getId(),
            link.getShortCode(),
            shortUrl,
            destinationUrl,
            link.getVisibility(),
            link.getCustomAlias(),
            link.getExpiresAt(),
            link.getMaxUses(),
            link.getUseCount(),
            link.isActive(),
            link.getMetadata(),
            link.getCreatedAt()
        );
    }
}