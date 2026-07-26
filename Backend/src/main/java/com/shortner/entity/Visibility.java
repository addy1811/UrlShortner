package com.shortner.entity;

/**
 * Controls who can resolve a short link at redirect time.
 *
 * PUBLIC     - anyone with the short URL can use it.
 * PRIVATE    - only the owner can use/view it (e.g. personal bookmarking).
 * RESTRICTED - only users/emails explicitly granted access (see {@link LinkAccessGrant})
 *              can resolve it. This is the "share only with people I choose" mode.
 */
public enum Visibility {
    PUBLIC,
    PRIVATE,
    RESTRICTED
}
 