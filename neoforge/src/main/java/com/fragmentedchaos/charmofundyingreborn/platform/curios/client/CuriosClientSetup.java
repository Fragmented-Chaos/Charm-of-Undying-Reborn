package com.fragmentedchaos.charmofundyingreborn.platform.curios.client;

import com.fragmentedchaos.charmofundyingreborn.platform.curios.client.TotemCurioRenderer;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Curios-only client setup: registers the 3D totem renderer for the vanilla totem of undying.
 * <p>
 * Kept in its own class so no Curios class is resolved on an instance that only has Trinkets
 * Updated installed.
 */
public final class CuriosClientSetup {

    private CuriosClientSetup() {
    }

    public static void register() {
        ICurioRenderer.register(Items.TOTEM_OF_UNDYING, () -> new TotemCurioRenderer());
    }
}
