package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.common.network.ClientTotemHandler;
import com.fragmentedchaos.charmofundyingreborn.common.network.TotemUsePayload;
import com.fragmentedchaos.charmofundyingreborn.platform.services.INetworkHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FabricNetworkHelper implements INetworkHelper {

    @Override
    public void sendTotemUse(ServerPlayer player, ItemStack stack) {
        TotemUsePayload payload = new TotemUsePayload(player.getId(), stack);
        for (ServerPlayer tracker : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(tracker, payload);
        }
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void registerPayloadType() {
        // Required on both server (to send) and client (to receive)
        PayloadTypeRegistry.clientboundPlay().register(TotemUsePayload.TYPE, TotemUsePayload.STREAM_CODEC);
    }

    @Override
    public void registerClientHandler() {
        ClientPlayNetworking.registerGlobalReceiver(TotemUsePayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        ClientTotemHandler.handle(payload)));
    }
}
