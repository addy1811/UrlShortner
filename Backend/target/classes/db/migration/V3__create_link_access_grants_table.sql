CREATE TABLE access_grants (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id             UUID NOT NULL,
    grantee_user_id     UUID,
    invited_email       VARCHAR(255),
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    granted_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_link FOREIGN KEY (link_id) REFERENCES short_links (id) ON DELETE CASCADE,
    CONSTRAINT fk_grantee FOREIGN KEY (grantee_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'REVOKED')),
    -- Either a registered user or an invited email must be present, not neither
    CONSTRAINT chk_target CHECK (grantee_user_id IS NOT NULL OR invited_email IS NOT NULL),
    CONSTRAINT uq_grantee UNIQUE (link_id, grantee_user_id)
);
CREATE INDEX idx_link_id ON access_grants (link_id);
CREATE INDEX idx_grantee_user_id ON access_grants (grantee_user_id);
CREATE INDEX idx_invited_email ON access_grants (invited_email);