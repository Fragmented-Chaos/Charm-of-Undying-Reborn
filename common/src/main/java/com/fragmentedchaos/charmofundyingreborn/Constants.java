package com.fragmentedchaos.charmofundyingreborn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants for Charm of Undying: Reborn.
 * Mod ID, name, and authors are populated at build time via gradle.properties expansion.
 */
public final class Constants {

    public static final String MOD_ID = "charmofundyingreborn";
    public static final String MOD_NAME = "Charm of Undying: Reborn";
    public static final String MOD_AUTHORS = "Fragmented_Chaos";

    public static final String CHARM_SLOT_ID = "charm";

    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
