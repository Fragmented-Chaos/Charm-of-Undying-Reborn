package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.common.TotemProviders;

/** 两端共用的初始化入口，由各加载器的入口点调用。 */
public final class CharmOfUndyingRebornCommon {

    private static boolean initialized = false;

    private CharmOfUndyingRebornCommon() {
        throw new UnsupportedOperationException("Cannot instantiate CharmOfUndyingRebornCommon");
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        Constants.LOG.info("Initializing {} by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);
        TotemProviders.init();
    }
}
