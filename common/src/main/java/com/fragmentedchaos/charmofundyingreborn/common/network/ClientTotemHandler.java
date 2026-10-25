package com.fragmentedchaos.charmofundyingreborn.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/** 客户端处理图腾复活包，播第一人称的图腾动画。 */
public final class ClientTotemHandler {

    private ClientTotemHandler() {}

    public static void handle(TotemUsePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(payload.entityId());
        // 包里广播给了所有追踪该玩家的客户端，但动画是第一人称的，只在本地玩家时播。
        if (entity == mc.player) {
            mc.gameRenderer.displayItemActivation(payload.stack());
        }
    }
}
