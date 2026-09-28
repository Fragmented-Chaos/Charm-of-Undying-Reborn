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

/**
 * NeoForge entry point for Charm of Undying: Reborn.
 * <p>
 * The charm slot works with either Curios or Trinkets Updated; the provider is chosen at runtime in
 * {@link NeoForgeCharmSlotHelper}. Death handling is managed via CharmSlotTotemMixin.
 */
@Mod(Constants.MOD_ID)
public class CharmOfUndyingReborn {

    public CharmOfUndyingReborn(IEventBus eventBus, ModContainer container) {
        Constants.LOG.info("Starting {} on NeoForge by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        requireAccessoryProvider(container);

        // The universal jar ships both loaders' implementations; install this side's services.
        CharmSlotServices.install(
                new NeoForgeCharmSlotHelper(),
                new NeoForgeNetworkHelper(),
                new NeoForgeTotemUseGuard());

        CharmOfUndyingRebornCommon.init();
        eventBus.addListener(NeoForgeNetworkHelper::onRegisterPayloads);
        // Register totem ICurioItems only once the server is fully started: creating default
        // ItemStacks requires DataComponents to be bound and item tags to be loaded, which is
        // guaranteed at this point. Skipped when Curios is absent - Trinkets Updated needs no
        // registration because its slots and item tags are plain data files.
        NeoForge.EVENT_BUS.addListener(CharmOfUndyingReborn::onServerStarted);
        ChorCommand.register();

        Constants.LOG.info("{} successfully initialized on NeoForge", Constants.MOD_NAME);
    }

    /**
     * Both accessory mods are declared {@code optional} in neoforge.mods.toml because NeoForge has
     * no way to express "one of", so the requirement is enforced here instead: without Curios or
     * Trinkets Updated there is no charm slot at all, and silently degrading would just look like a
     * broken mod. Reported as a loading error, which aborts startup with a readable message.
     */
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
