package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.curios.client.CuriosClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** 客户端初始化：装了 Curios 就注册图腾 3D 渲染器（只针对原版图腾）。 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        if (ModList.get().isLoaded("curios")) {
            CuriosClientSetup.register();
        }
    }
}
