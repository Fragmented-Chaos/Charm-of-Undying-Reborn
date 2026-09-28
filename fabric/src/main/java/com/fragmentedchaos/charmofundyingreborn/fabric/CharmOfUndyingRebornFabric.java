package com.fragmentedchaos.charmofundyingreborn.fabric;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.CharmOfUndyingRebornCommon;

import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.fragmentedchaos.charmofundyingreborn.fabric.FabricNetworkHelper;
import com.fragmentedchaos.charmofundyingreborn.fabric.FabricTotemUseGuard;
import com.fragmentedchaos.charmofundyingreborn.platform.trinkets.TrinketsCharmSlotHelper;
import dev.yumi.commons.TriState;
import eu.pb4.trinkets.api.event.TrinketSlotCompatibilityCallback;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point for Charm of Undying: Reborn.
 * <p>
 * Named {@code ...Fabric} so that it can live next to the NeoForge {@code @Mod} class inside the
 * universal jar (both loaders read their own metadata from that one file). Death handling is
 * managed via CharmSlotTotemMixin.
 */
public class CharmOfUndyingRebornFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Starting {} on Fabric by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        // The universal jar contains both loaders' implementations, so this side installs its own
        // instead of relying on META-INF/services.
        CharmSlotServices.install(
                new TrinketsCharmSlotHelper(),
                new FabricNetworkHelper(),
                new FabricTotemUseGuard());

        CharmOfUndyingRebornCommon.init();

        // Register payload type on both sides (server sends, client receives)
        CharmSlotServices.network().registerPayloadType();

        // Make totem items compatible with all trinket slots
        TrinketSlotCompatibilityCallback.EVENT.register(
                (stack, slotAccess, entity, isEquipped) ->
                        TotemHelper.isTotem(stack) ? TriState.TRUE : TriState.DEFAULT
        );

        Constants.LOG.info("{} successfully initialized on Fabric", Constants.MOD_NAME);
    }
}
