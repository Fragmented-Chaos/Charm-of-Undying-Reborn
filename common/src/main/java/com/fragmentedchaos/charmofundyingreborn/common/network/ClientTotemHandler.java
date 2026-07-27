package com.fragmentedchaos.charmofundyingreborn.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Client-side handler for TotemUsePayload.
 * Renders the totem activation animation with the specific item icon.
 */
public final class ClientTotemHandler {

    private ClientTotemHandler() {}

    public static void handle(TotemUsePayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(payload.entityId());
        if (entity != null) {
            mc.gameRenderer.displayItemActivation(payload.stack());
        }
    }
}
