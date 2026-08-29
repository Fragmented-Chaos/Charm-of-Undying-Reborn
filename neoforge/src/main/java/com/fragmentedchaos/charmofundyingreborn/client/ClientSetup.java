package com.fragmentedchaos.charmofundyingreborn.client;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Client-only setup. Registers the {@link TotemCurioRenderer} for the vanilla totem of undying so
 * it renders its 3D model on the player's body when equipped in a Curios "charm" slot.
 *
 * <p>Rendering is intentionally limited to the vanilla totem (no tag-based registration), keeping
 * client code minimal and avoiding any client-class-on-server concerns.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientSetup {

    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        ICurioRenderer.register(Items.TOTEM_OF_UNDYING, () -> new TotemCurioRenderer());
    }
}
