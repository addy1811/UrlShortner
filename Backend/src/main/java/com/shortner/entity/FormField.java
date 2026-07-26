package com.shortner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "form_fields",
    uniqueConstraints = @UniqueConstraint(columnNames = {"link_id", "field_key"})
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private ShortLink link;

    // Machine-readable key used as the JSON key in FormResponse.responseData, e.g. "phoneNumber".
    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    // Human-readable label shown on the rendered form, e.g. "Phone Number".
    @Column(nullable = false, length = 255)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(name = "is_required", nullable = false)
    @Builder.Default
    private boolean required = false;

    // Only populated for DROPDOWN / CHECKBOX field types, e.g. ["Small","Medium","Large"].
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> options;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}