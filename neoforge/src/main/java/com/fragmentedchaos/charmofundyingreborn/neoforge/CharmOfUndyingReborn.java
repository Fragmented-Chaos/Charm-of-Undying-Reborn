package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.CharmOfUndyingRebornCommon;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.fragmentedchaos.charmofundyingreborn.neoforge.NeoForgeCharmSlotHelper;
import com.fragmentedchaos.charmofundyingreborn.neoforge.NeoForgeNetworkHelper;
import com.fragmentedchaos.charmofundyingreborn.neoforge.NeoForgeTotemUseGuard;
import com.fragmentedchaos.charmofundyingreborn.platform.curios.CuriosTotemRegistration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** NeoForge 入口点。死亡处理由 CharmSlotTotemMixin 完成。 */
@Mod(Constants.MOD_ID)
public class CharmOfUndyingReborn {

    public CharmOfUndyingReborn(IEventBus eventBus, ModContainer container) {
        Constants.LOG.info("Starting {} on NeoForge by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        requireAccessoryProvider(container);

        // 通用单包里同时有两端的实现，各自安装自己那份。
        CharmSlotServices.install(
                new NeoForgeCharmSlotHelper(),
                new NeoForgeNetworkHelper(),
                new NeoForgeTotemUseGuard());

        CharmOfUndyingRebornCommon.init();
        eventBus.addListener(NeoForgeNetworkHelper::onRegisterPayloads);
        // 等到服务端启动后才注册 ICurioItem：那时 DataComponents 和物品标签才都可用。
        NeoForge.EVENT_BUS.addListener(CharmOfUndyingReborn::onServerStarted);
        ChorCommand.register();

        Constants.LOG.info("{} successfully initialized on NeoForge", Constants.MOD_NAME);
    }

    /** Curios 与 Trinkets 在 toml 里都只能标 optional，所以"至少要有一个"在这里强制。 */
    private static void requireAccessoryProvider(ModContainer container) {
        if (ModList.get().isLoaded(NeoForgeCharmSlotHelper.CURIOS)
                || ModList.get().isLoaded(NeoForgeCharmSlotHelper.TRINKETS)) {
            return;
        }
        ModLoader.addLoadingIssue(ModLoadingIssue.error(
                "Charm of Undying: Reborn requires an accessory mod on NeoForge: install either "
                        + "Curios API or Trinkets Updated (neither was found).")
                .withAffectedMod(container.getModInfo()));
    }

    private static void onServerStarted(ServerStartedEvent event) {
        if (ModList.get().isLoaded(NeoForgeCharmSlotHelper.CURIOS)) {
            CuriosTotemRegistration.registerTotemCurios();
        }
    }
}
