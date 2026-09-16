package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.INetworkHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;

import java.util.ServiceLoader;

/**
 * Service loader for the platform-specific charm slot helper.
 */
public final class CharmSlotServices {

    /**
     * The loaded platform-specific charm slot helper implementation.
     */
    public static final ICharmSlotHelper CHARM_SLOT = load(ICharmSlotHelper.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    /**
     * Optional platform hook letting other mods veto a charm-slot resurrection.
     * {@code null} when the platform offers no such hook.
     */
    public static final ITotemUseGuard TOTEM_GUARD = loadOptional(ITotemUseGuard.class);

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

    /**
     * Loads a service that a platform is allowed not to provide, returning {@code null} instead of
     * failing the whole mod when it is absent.
     */
    public static <T> T loadOptional(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, CharmSlotServices.class.getClassLoader())
                .findFirst()
                .orElse(null);
        if (loadedService != null) {
            Constants.LOG.debug("Loaded {} for optional service {}", loadedService, clazz);
        }
        return loadedService;
    }
}
