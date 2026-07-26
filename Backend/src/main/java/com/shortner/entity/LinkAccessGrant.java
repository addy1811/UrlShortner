package com.shortner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "access_grants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"link_id", "grantee_user_id"})
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkAccessGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private ShortLink link;

    // Nullable: an invite can target an email before the person has an account.
    // Gets populated once the invited email registers/claims the grant.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grantee_user_id")
    private User grantee;

    @Column(name = "invited_email", length = 255)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private GrantStatus status = GrantStatus.PENDING;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    @PrePersist
    void onCreate() {
        if (grantedAt == null) {
            grantedAt = Instant.now();
        }
    }
}