package com.fragmentedchaos.charmofundyingreborn.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Defines custom totem behavior for items in the charm slot.
 * <p>
 * Register via {@link TotemProviders#register(String, ITotemEffect)}.
 * Items tagged with {@code c:totems} automatically use the vanilla effect.
 */
public interface ITotemEffect {

    /** Whether this totem can bypass /kill and void damage. */
    default boolean bypassInvul() {
        return false;
    }

    /**
     * Modifies the item stack to consume the totem.
     * Default: shrinks by 1. Override for custom consumption (e.g. durability).
     */
    void modifyStack(ItemStack stack);

    /**
     * Applies the totem resurrection effects.
     * @return true if effects were applied successfully
     */
    boolean applyEffects(Player player);
}
