package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.common.network.ClientTotemHandler;
import com.fragmentedchaos.charmofundyingreborn.common.network.TotemUsePayload;
import com.fragmentedchaos.charmofundyingreborn.platform.services.INetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeNetworkHelper implements INetworkHelper {

    @Override
    public void sendTotemUse(ServerPlayer player, ItemStack stack) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player, new TotemUsePayload(player.getId(), stack));
    }

    @Override
    public void registerClientHandler() {
        // NeoForge handler is registered via event listener in mod constructor
    }

    /** Called from the NeoForge mod constructor via event bus. */
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(TotemUsePayload.TYPE, TotemUsePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() ->
                        ClientTotemHandler.handle(payload)));
    }
}
