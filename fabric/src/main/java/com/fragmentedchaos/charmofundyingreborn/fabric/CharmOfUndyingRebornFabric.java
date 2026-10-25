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

/** Fabric 入口点。类名带 Fabric 后缀是为了和 NeoForge 的 @Mod 类共存于同一个通用单包。 */
public class CharmOfUndyingRebornFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("Starting {} on Fabric by {}", Constants.MOD_NAME, Constants.MOD_AUTHORS);

        // 通用单包装了 compat 两端的实现，这里安装 Fabric 自己那份。
        CharmSlotServices.install(
                new TrinketsCharmSlotHelper(),
                new FabricNetworkHelper(),
                new FabricTotemUseGuard());

        CharmOfUndyingRebornCommon.init();

        CharmSlotServices.network().registerPayloadType();

        TrinketSlotCompatibilityCallback.EVENT.register(
                (stack, slotAccess, entity, isEquipped) ->
                        TotemHelper.isTotem(stack) ? TriState.TRUE : TriState.DEFAULT
        );

        Constants.LOG.info("{} successfully initialized on Fabric", Constants.MOD_NAME);
    }
}
