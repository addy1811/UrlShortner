CREATE TABLE short_links (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    short_code              VARCHAR(10) NOT NULL,
    encrypted_destination   BYTEA NOT NULL,
    encryption_iv           BYTEA NOT NULL,
    owner_id                UUID NOT NULL,
    visibility              VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    custom_alias            VARCHAR(50),
    expires_at              TIMESTAMP WITH TIME ZONE,
    max_uses                INTEGER,
    use_count               INTEGER NOT NULL DEFAULT 0,
    is_active               BOOLEAN NOT NULL DEFAULT true,
    metadata                JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT uq_shortcode UNIQUE (short_code),
    CONSTRAINT uq_custom_alias UNIQUE (custom_alias),
    CONSTRAINT fk_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'RESTRICTED')),
    CONSTRAINT chk_max_uses CHECK (max_uses IS NULL OR max_uses > 0)
);

CREATE INDEX idx_owner_id ON short_links (owner_id);
CREATE INDEX idx_short_code ON short_links (short_code);

-- Lets you query/filter on metadata fields (tags, description) without a full table scan
CREATE INDEX idx_metadata ON short_links USING GIN (metadata);