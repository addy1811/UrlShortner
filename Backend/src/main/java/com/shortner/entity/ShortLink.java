package com.shortner.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
 
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
 
@Entity
@Table(name = "short_links")
@Getter
@Setter
@ToString(exclude = {"encryptedDestination", "encryptionIv"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortLink {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
 
    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;
 
    // Ciphertext only - decrypted on read by EncryptionService, never exposed via DTO directly.
    @Column(name = "encrypted_destination", nullable = false)
    private byte[] encryptedDestination;
 
    // AES-GCM IV, generated fresh per encryption, stored alongside the ciphertext.
    @Column(name = "encryption_iv", nullable = false)
    private byte[] encryptionIv;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;
 
    @Column(name = "custom_alias", unique = true, length = 50)
    private String customAlias;
 
    @Column(name = "expires_at")
    private Instant expiresAt;
 
    @Column(name = "max_uses")
    private Integer maxUses;
 
    @Column(name = "use_count", nullable = false)
    @Builder.Default
    private int useCount = 0;
 
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
 
    // Hibernate 7 maps Map<String,Object> -> Postgres jsonb natively, no extra
    // dependency (hypersistence-utils) needed like it was pre-Boot-4.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
 
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
 
    /** True if the link has hit its max-use limit or passed its expiry timestamp. */
    @Transient
    public boolean isExpiredOrExhausted() {
        boolean pastExpiry = expiresAt != null && Instant.now().isAfter(expiresAt);
        boolean usedUp = maxUses != null && useCount >= maxUses;
        return pastExpiry || usedUp;
    }
}
 