package com.fragmentedchaos.charmofundyingreborn.platform.curios.client;

import com.fragmentedchaos.charmofundyingreborn.platform.curios.client.TotemCurioRenderer;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/** Curios 客户端初始化：只给原版图腾注册 3D 渲染器。单独成类，避免没装 Curios 时加载到 Curios 的类。 */
public final class CuriosClientSetup {

    private CuriosClientSetup() {
    }

    public static void register() {
        ICurioRenderer.register(Items.TOTEM_OF_UNDYING, () -> new TotemCurioRenderer());
    }
}
