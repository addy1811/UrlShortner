package com.shortner.repository;

import com.shortner.entity.GrantStatus;
import com.shortner.entity.LinkAccessGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkAccessGrantRepository extends JpaRepository<LinkAccessGrant, UUID> {
 @Query("SELECT g FROM LinkAccessGrant g LEFT JOIN FETCH g.grantee WHERE g.link.id = :linkId")
    // JOIN FETCH loads the grantee User row in this same query, while the
    // transaction is still open - without it, AccessGrantController's DTO
    // mapping (grant.getGrantee().getUsername()) runs AFTER the transaction
    // closes and throws LazyInitializationException trying to lazy-load a
    // proxy with no session left. We deliberately don't use Open-Session-In-View
    // (see application.yml's jpa.open-in-view: false) to fix this the other way -
    // the right fix is loading what you need up front, not keeping sessions open longer.
    List<LinkAccessGrant> findByLinkId(@Param("linkId") UUID linkId);
     
     @Query("SELECT g FROM LinkAccessGrant g LEFT JOIN FETCH g.grantee WHERE g.id = :grantId")
    Optional<LinkAccessGrant> findByIdWithGrantee(@Param("grantId") UUID grantId);
    
    // The core access-check for RESTRICTED links: does this user have an ACTIVE grant?
    Optional<LinkAccessGrant> findByLinkIdAndGranteeIdAndStatus(UUID linkId, UUID granteeId, GrantStatus status);

    // Used when an invited (not-yet-registered) email finally signs up, to attach
    // any pending grants to their new account.
    List<LinkAccessGrant> findByInvitedEmailAndStatus(String invitedEmail, GrantStatus status);

    boolean existsByLinkIdAndGranteeId(UUID linkId, UUID granteeId);

    @Query("""
        SELECT g FROM LinkAccessGrant g
        WHERE g.link.id = :linkId
          AND g.grantee.id = :granteeId
        """)
    Optional<LinkAccessGrant> findByLinkAndGrantee(@Param("linkId") UUID linkId, @Param("granteeId") UUID granteeId);
}