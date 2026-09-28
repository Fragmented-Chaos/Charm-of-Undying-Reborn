package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.platform.curios.client.CuriosClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client-only setup. Registers the 3D totem renderer when Curios is installed.
 * <p>
 * Rendering is intentionally limited to the vanilla totem (no tag-based registration), keeping
 * client code minimal and avoiding any client-class-on-server concerns.
 * <p>
 * A Trinkets-side renderer is not registered yet; Trinkets Updated exposes its own rendering API.
 */
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
