package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.INetworkHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;

/**
 * Holds the platform-specific service implementations.
 * <p>
 * Each loader entry point installs its own implementations during initialization. This replaces the
 * usual {@code META-INF/services} + {@link java.util.ServiceLoader} approach because the released
 * artifact is a single universal jar that carries the Fabric and the NeoForge implementations at
 * the same time, and one jar cannot contain two service files for the same interface. It also keeps
 * {@code common} free of references to loader-specific classes, which would not compile otherwise.
 */
public final class CharmSlotServices {

    private static volatile ICharmSlotHelper charmSlot;
    private static volatile INetworkHelper network;
    private static volatile ITotemUseGuard totemGuard;

    private CharmSlotServices() {
        throw new UnsupportedOperationException("CharmSlotServices cannot be instantiated");
    }

    /**
     * Called once by the loader entry point, before anything touches the services.
     *
     * @param charmSlotHelper the accessory slot provider for this loader
     * @param networkHelper   the networking implementation for this loader
     * @param guard           optional resurrection veto hook, {@code null} if the loader has none
     */
    public static synchronized void install(ICharmSlotHelper charmSlotHelper,
                                            INetworkHelper networkHelper,
                                            ITotemUseGuard guard) {
        if (charmSlot != null) {
            Constants.LOG.warn("Platform services are already installed; ignoring the second install");
            return;
        }
        charmSlot = charmSlotHelper;
        network = networkHelper;
        totemGuard = guard;
        Constants.LOG.debug("Installed platform services (charm slot provider: {}, totem guard: {})",
                charmSlotHelper.getPlatformName(), guard != null);
    }

    public static ICharmSlotHelper charmSlot() {
        return require(charmSlot, "charm slot helper");
    }

    public static INetworkHelper network() {
        return require(network, "network helper");
    }

    /**
     * @return the platform's veto hook, or {@code null} when the loader offers no such hook
     */
    public static ITotemUseGuard totemGuard() {
        return totemGuard;
    }

    private static <T> T require(T service, String what) {
        if (service == null) {
            throw new IllegalStateException("Platform services are not installed yet (" + what
                    + "); the loader entry point has to call CharmSlotServices.install(...) first");
        }
        return service;
    }
}
