package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.ModTags;

import net.minecraft.world.item.ItemStack;

/**
 * Utility class for identifying totem items.
 * Checks the "c:totems" item tag; any item carrying that tag is treated as a totem.
 */
public final class TotemHelper {

    private TotemHelper() {
        throw new UnsupportedOperationException("TotemHelper cannot be instantiated");
    }

    /**
     * Checks whether the given ItemStack should be treated as a totem of undying.
     *
     * @param stack The ItemStack to check
     * @return true if the item carries the {@code c:totems} tag
     */
    public static boolean isTotem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ModTags.TOTEMS);
    }
}
