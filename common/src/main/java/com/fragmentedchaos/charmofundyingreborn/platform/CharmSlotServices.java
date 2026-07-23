package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;

import java.util.ServiceLoader;

/**
 * Service loader for the platform-specific charm slot helper.
 */
public final class CharmSlotServices {

    /**
     * The loaded platform-specific charm slot helper implementation.
     */
    public static final ICharmSlotHelper CHARM_SLOT = load(ICharmSlotHelper.class);

    private CharmSlotServices() {
        throw new UnsupportedOperationException("CharmSlotServices cannot be instantiated");
    }

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, CharmSlotServices.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException(
                        "Failed to load service for " + clazz.getName()
                                + ". Ensure Trinkets (Fabric) or Curios (NeoForge) is installed."));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
