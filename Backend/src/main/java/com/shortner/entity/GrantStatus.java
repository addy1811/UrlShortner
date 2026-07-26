package com.shortner.entity;

/**
 * Lifecycle state of a single access grant on a RESTRICTED short link.
 *
 * PENDING - grant created (often via invited email) but the invitee hasn't
 *           registered/claimed it yet.
 * ACTIVE  - grantee can currently resolve the link.
 * REVOKED - owner pulled access; must be re-granted to restore it.
 */
public enum GrantStatus {
    PENDING,
    ACTIVE,
    REVOKED
}
 