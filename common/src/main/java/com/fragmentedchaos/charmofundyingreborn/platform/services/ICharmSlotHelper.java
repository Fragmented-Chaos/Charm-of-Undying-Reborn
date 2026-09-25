package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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

    /**
     * Applies {@code action} to the totem in the charm slot and writes the result back into the
     * same slot.
     * <p>
     * Needed because a platform may hand out a detached copy: Curios 17 on NeoForge rebuilds an
     * {@link ItemStack} from its resource handler on every read, so mutating the stack returned by
     * {@link #getCharmSlot(Player)} would not touch the real inventory.
     *
     * @param player The player whose charm slot holds the totem
     * @param action Receives the stack to modify; emptying it removes the totem
     */
    void modifyCharmSlot(Player player, Consumer<ItemStack> action);
}
