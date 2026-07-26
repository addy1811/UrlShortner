CREATE TABLE access_logs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id             UUID NOT NULL,
    accessed_by_user_id UUID,
    ip_hash             VARCHAR(64),   -- SHA-256 hex digest, never raw IP
    accessed_at         TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    access_granted      BOOLEAN NOT NULL,

    CONSTRAINT fk_access_logs_link FOREIGN KEY (link_id) REFERENCES short_links (id) ON DELETE CASCADE,
    CONSTRAINT fk_access_logs_user FOREIGN KEY (accessed_by_user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_access_logs_link_id ON access_logs (link_id);
CREATE INDEX idx_access_logs_accessed_at ON access_logs (accessed_at);
-- Speeds up "show me denied attempts on my link" queries for the owner's security view
CREATE INDEX idx_access_logs_denied ON access_logs (link_id, access_granted) WHERE access_granted = false;