package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import dev.yumi.commons.TriState;
import eu.pb4.trinkets.api.event.TrinketSlotCompatibilityCallback;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric entry point for Charm of Undying: Reborn.
 * Death handling is managed via CharmSlotTotemMixin.
 */
public class CharmOfUndyingReborn implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Starting {} on Fabric by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        CharmOfUndyingRebornCommon.init();

        // Register payload type on both sides (server sends, client receives)
        CharmSlotServices.NETWORK.registerPayloadType();

        // Make totem items compatible with all trinket slots
        TrinketSlotCompatibilityCallback.EVENT.register(
                (stack, slotAccess, entity, isEquipped) ->
                        TotemHelper.isTotem(stack) ? TriState.TRUE : TriState.DEFAULT
        );

        Constants.LOG.info("{} successfully initialized on Fabric", Constants.MOD_NAME);
    }
}
