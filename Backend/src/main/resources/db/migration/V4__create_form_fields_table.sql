CREATE TABLE form_fields (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id         UUID NOT NULL,
    field_key       VARCHAR(50) NOT NULL,
    label           VARCHAR(255) NOT NULL,
    field_type      VARCHAR(20) NOT NULL,
    is_required     BOOLEAN NOT NULL DEFAULT false,
    options         JSONB,
    display_order   INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_form_fields_link FOREIGN KEY (link_id) REFERENCES short_links (id) ON DELETE CASCADE,
    CONSTRAINT chk_form_fields_type CHECK (field_type IN ('TEXT', 'NUMBER', 'EMAIL', 'DATE', 'DROPDOWN', 'CHECKBOX')),
    CONSTRAINT uq_form_fields_link_key UNIQUE (link_id, field_key)
);

CREATE INDEX idx_form_link_id ON form_fields (link_id);