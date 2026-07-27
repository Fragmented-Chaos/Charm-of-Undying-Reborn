package com.fragmentedchaos.charmofundyingreborn.common.network;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TotemUsePayload(int entityId, ItemStack stack) implements CustomPacketPayload {

    public static final Type<TotemUsePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "totem_use"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TotemUsePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, TotemUsePayload::entityId,
                    ItemStack.OPTIONAL_STREAM_CODEC, TotemUsePayload::stack,
                    TotemUsePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
