package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.ModTags;

import net.minecraft.world.item.ItemStack;

/** 判断物品是不是图腾（带 {@code c:totems} 标签即算）。 */
public final class TotemHelper {

    private TotemHelper() {
        throw new UnsupportedOperationException("TotemHelper cannot be instantiated");
    }

    public static boolean isTotem(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ModTags.TOTEMS);
    }
}
