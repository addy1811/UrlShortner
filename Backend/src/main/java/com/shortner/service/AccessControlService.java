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
import com.shortner.exception.PendingInviteException;
import com.shortner.repository.LinkAccessGrantRepository;
import com.shortner.repository.ShortLinkRepository;
import com.shortner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final LinkAccessGrantRepository linkAccessGrantRepository;
    private final ShortLinkRepository shortLinkRepository;
    private final UserRepository userRepository;

    public void assertAccessAllowed(ShortLink link, UUID requestingUserId) {
        if (!link.isActive()) {
            throw new LinkNotFoundException("This link is no longer active");
        }

        if (link.isExpiredOrExhausted()) {
            throw new LinkExpiredException("This link has expired or reached its usage limit");
        }

        boolean isOwner = requestingUserId != null && requestingUserId.equals(link.getOwner().getId());
        if (isOwner) {
            return; 
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


    @Transactional
   public LinkAccessGrant grantAccess(UUID ownerId, UUID linkId, GrantAccessRequest request) {
    ShortLink link = shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
        .orElseThrow(() -> new LinkNotFoundException("Link not found"));

    LinkAccessGrant.LinkAccessGrantBuilder builder = LinkAccessGrant.builder().link(link);

    if (request.username() != null) {
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

    if (!grant.getLink().getId().equals(linkId)) {
        throw new LinkNotFoundException("Grant not found");
    }

    grant.setStatus(GrantStatus.REVOKED);
    linkAccessGrantRepository.save(grant);
}
@Transactional
public LinkAccessGrant reactivateAccess(UUID ownerId, UUID linkId, UUID grantId) {
    shortLinkRepository.findByIdAndOwnerId(linkId, ownerId)
        .orElseThrow(() -> new LinkNotFoundException("Link not found"));

    LinkAccessGrant grant = linkAccessGrantRepository.findByIdWithGrantee(grantId)
        .orElseThrow(() -> new LinkNotFoundException("Grant not found"));

    if (!grant.getLink().getId().equals(linkId)) {
        throw new LinkNotFoundException("Grant not found");
    }

    if (grant.getGrantee() == null) {
        throw new PendingInviteException("This invite is still pending - it will activate once the invitee registers");
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