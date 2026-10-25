package com.fragmentedchaos.charmofundyingreborn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 常量。模组 id、名称、作者在构建时由 gradle.properties 展开。 */
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
