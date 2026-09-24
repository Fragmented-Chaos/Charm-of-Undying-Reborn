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
        // The payload is broadcast to every tracking client, but the activation animation is a
        // first-person overlay. Vanilla scopes it the same way in
        // ClientPacketListener#handleEntityEvent (case 35): only when the affected entity is the
        // local player. Without this check, another player's totem would pop up on your screen.
        //
        // 26.3 moved the entry point from GameRenderer/ScreenEffectRenderer to LocalPlayer.
        if (entity == mc.player) {
            mc.player.displayItemActivation(payload.stack());
        }
    }
}
