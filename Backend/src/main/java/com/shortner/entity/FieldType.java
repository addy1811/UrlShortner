package com.shortner.entity;

/**
 * Supported input types a link owner can choose when building their custom
 * data-collection form. DROPDOWN and CHECKBOX rely on {@link FormField#getOptions()}
 * being populated.
 */
public enum FieldType {
    TEXT,
    NUMBER,
    EMAIL,
    DATE,
    DROPDOWN,
    CHECKBOX
}
 