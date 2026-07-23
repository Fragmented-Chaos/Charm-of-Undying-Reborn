package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * Platform-abstracted service for accessing the "charm" accessory slot.
 * Fabric uses Trinkets, NeoForge uses Curios — the common code works
 * with this interface regardless of platform.
 */
public interface ICharmSlotHelper {

    /**
     * Gets the name of the platform providing the charm slot.
     *
     * @return Platform name (e.g. "Trinkets" or "Curios")
     */
    String getPlatformName();

    /**
     * Retrieves the item currently in the player's charm slot.
     *
     * @param player The player to query
     * @return The ItemStack in the charm slot, or ItemStack.EMPTY if none / slot unavailable
     */
    @Nullable
    ItemStack getCharmSlot(Player player);

    /**
     * Checks whether the player has a charm slot available.
     *
     * @param player The player to check
     * @return true if the charm slot exists and is accessible
     */
    boolean hasCharmSlot(Player player);
}
