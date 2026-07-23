package com.fragmentedchaos.charmofundyingreborn;

/**
 * Common initialization entry point for Charm of Undying: Reborn.
 * Platform-specific entry points (Fabric ModInitializer / NeoForge @Mod)
 * call this to set up shared configuration.
 */
public final class CharmOfUndyingRebornCommon {

    private static boolean initialized = false;

    private CharmOfUndyingRebornCommon() {
        throw new UnsupportedOperationException("Cannot instantiate CharmOfUndyingRebornCommon");
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        Constants.LOG.info("Initializing {} by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);
        ModConfig.load();
    }
}
