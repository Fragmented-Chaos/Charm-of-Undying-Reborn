package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.INetworkHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;

/**
 * 存放各加载器的服务实现，由入口点在初始化时安装。
 * <p>
 * 不用 {@code META-INF/services} + ServiceLoader：发布的是通用单包，
 * 一个 jar 装不下两份同名服务文件；而且 common 里也不能引用加载器专属类。
 */
public final class CharmSlotServices {

    private static volatile ICharmSlotHelper charmSlot;
    private static volatile INetworkHelper network;
    private static volatile ITotemUseGuard totemGuard;

    private CharmSlotServices() {
        throw new UnsupportedOperationException("CharmSlotServices cannot be instantiated");
    }

    /** @param guard 复活否决钩子，加载器没有就是 null */
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

    /** @return 平台否决钩子，没有则返回 null */
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
