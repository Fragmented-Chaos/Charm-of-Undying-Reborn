package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Platform-abstracted networking for sending totem-use packets
 * from server to clients.
 */
public interface INetworkHelper {

    /** Sends the totem-use animation packet to all players tracking the given player. */
    void sendTotemUse(ServerPlayer player, ItemStack stack);

    /** Called by platform client init to register the packet receiver. */
    void registerClientHandler();
}
