CREATE TABLE form_responses (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    link_id                 UUID NOT NULL,
    submitted_by_user_id    UUID,
    response_data           JSONB NOT NULL,
    submitted_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_form_responses_link FOREIGN KEY (link_id) REFERENCES short_links (id) ON DELETE CASCADE,
    CONSTRAINT fk_form_responses_user FOREIGN KEY (submitted_by_user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_form_responses_link_id ON form_responses (link_id);
CREATE INDEX idx_form_responses_data ON form_responses USING GIN (response_data);