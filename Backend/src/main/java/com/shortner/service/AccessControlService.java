package com.shortner.service;

import com.shortner.dto.link.GrantAccessRequest;
import com.shortner.entity.GrantStatus;
import com.shortner.entity.LinkAccessGrant;
import com.shortner.entity.ShortLink;
import com.shortner.entity.User;
import com.shortner.exception.AccessDeniedException;
import com.shortner.exception.DuplicateAliasException;
import com.shortner.exception.LinkExpiredException;
import com.shortner.exception.LinkNotFoundException;
import com.shortner.repository.LinkAccessGrantRepository;
import com.shortner.repository.ShortLinkRepository;
import com.shortner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Single source of truth for "can this request touch this link" AND for managing
 * who's on the RESTRICTED allow-list. Every entry point that resolves or reads a
 * link (redirect, form schema, form submission, analytics) must go through
 * assertAccessAllowed rather than re-implementing visibility checks locally -
 * that duplication is exactly how access-control bugs creep in.
 */
@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final LinkAccessGrantRepository linkAccessGrantRepository;
    private final ShortLinkRepository shortLinkRepository;
    private final UserRepository userRepository;

    /**
     * Throws if the given (possibly anonymous, requestingUserId == null) requester
     * is not allowed to resolve/use this link right now. Returns normally if allowed.
     */
    public void assertAccessAllowed(ShortLink link, UUID requestingUserId) {
        if (!link.isActive()) {
            throw new LinkNotFoundException("This link is no longer active");
        }

        if (link.isExpiredOrExhausted()) {
            throw new LinkExpiredException("This link has expired or reached its usage limit");
        }

        boolean isOwner = requestingUserId != null && requestingUserId.equals(link.getOwner().getId());
        if (isOwner) {
            return; // owners always have access to their own links
        }

        switch (link.getVisibility()) {
            case PUBLIC -> {
                // no further check needed
            }
            case PRIVATE -> throw new AccessDeniedException("This link is private");
            case RESTRICTED -> {
                if (requestingUserId == null) {
                    throw new AccessDeniedException("You must be signed in to access this link");
                }
                boolean hasActiveGrant = linkAccessGrantRepository
                    .findByLinkIdAndGranteeIdAndStatus(link.getId(), requestingUserId, GrantStatus.ACTIVE)
                    .isPresent();
                if (!hasActiveGrant) {
                    throw new AccessDeniedException("You do not have access to this link");
                }
            }
        }
    }

    public boolean isOwner(ShortLink link, UUID userId) {
        return userId != null && userId.equals(link.getOwner().getId());
    }

    /** For ownership-gated write operations (edit/delete/view analytics) - stricter than read access. */
    public void assertIsOwner(ShortLink link, UUID userId) {
        if (!isOwner(link, userId)) {
            throw new AccessDeniedException("Only the link owner can perform this action");
        }
    }

    // ===== Grant management (RESTRICTED visibility allow-list) =====

    @Transactional
   public LinkAccessGrant grantAccess(UUID ownerId, UUID linkId, GrantAccessRequest request) {
    ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
        .orElseThrow(() -> new LinkNotFoundException("Link not found"));

    LinkAccessGrant.LinkAccessGrantBuilder builder = LinkAccessGrant.builder().link(link);

    if (request.username() != null) {
        // Username was explicitly provided - it must resolve to a real user,
        // there's no "invite by username" concept, only "invite by email".
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new LinkNotFoundException("No user found with username '" + request.username() + "'"));

        if (linkAccessGrantRepository.existsByLinkIdAndGranteeId(linkId, user.getId())) {
            throw new DuplicateAliasException("This user already has a grant on this link");
        }
        builder.grantee(user).status(GrantStatus.ACTIVE);
    } else {
        // Email path: an existing user with this email gets ACTIVE immediately;
        // otherwise it's a PENDING invite for when they eventually register.
        Optional<User> grantee = userRepository.findByEmail(request.email());
        if (grantee.isPresent()) {
            User user = grantee.get();
            if (linkAccessGrantRepository.existsByLinkIdAndGranteeId(linkId, user.getId())) {
                throw new DuplicateAliasException("This user already has a grant on this link");
            }
            builder.grantee(user).status(GrantStatus.ACTIVE);
        } else {
            builder.invitedEmail(request.email()).status(GrantStatus.PENDING);
        }
    }

    return linkAccessGrantRepository.save(builder.build());
}
    @Transactional
    public void revokeAccess(UUID ownerId, UUID linkId, UUID grantId) {
        shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        LinkAccessGrant grant = linkAccessGrantRepository.findById(grantId)
            .orElseThrow(() -> new LinkNotFoundException("Grant not found"));

        grant.setStatus(GrantStatus.REVOKED);
        linkAccessGrantRepository.save(grant);
    }
    
    @Transactional
public LinkAccessGrant reactivateAccess(UUID ownerId, UUID linkId, UUID grantId) {
    shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
        .orElseThrow(() -> new LinkNotFoundException("Link not found"));

    LinkAccessGrant grant = linkAccessGrantRepository.findByIdWithGrantee(grantId)
        .orElseThrow(() -> new LinkNotFoundException("Grant not found"));

    if (grant.getGrantee() == null) {
        // Pending email invites activate automatically when the invitee registers
        // (see AuthService) - there's nothing to manually toggle here yet.
        throw new IllegalStateException("This invite is still pending - it will activate once the invitee registers");
    }

    grant.setStatus(GrantStatus.ACTIVE);
    return linkAccessGrantRepository.save(grant);
}
    @Transactional(readOnly = true)
    public List<LinkAccessGrant> listGrants(UUID ownerId, UUID linkId) {
        shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
            .orElseThrow(() -> new LinkNotFoundException("Link not found"));

        return linkAccessGrantRepository.findByLinkId(linkId);
    }
}