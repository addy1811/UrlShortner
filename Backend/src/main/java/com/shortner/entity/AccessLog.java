package com.shortner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_logs")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private ShortLink link;

    // Nullable - anonymous/unauthenticated access attempts are still logged.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessed_by_user_id")
    private User accessedBy;

    // SHA-256 hex digest of the requester's IP - never store the raw address.
    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "accessed_at", nullable = false, updatable = false)
    private Instant accessedAt;

    @Column(name = "access_granted", nullable = false)
    private boolean accessGranted;

    @PrePersist
    void onCreate() {
        if (accessedAt == null) {
            accessedAt = Instant.now();
        }
    }
}