package com.fragmentedchaos.charmofundyingreborn;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for identifying totem items.
 * Primary method: checks the "c:totems" item tag.
 * Fallback method: checks the custom_totems.json config.
 */
public final class TotemHelper {

    private TotemHelper() {
        throw new UnsupportedOperationException("TotemHelper cannot be instantiated");
    }

    /**
     * Checks whether the given ItemStack should be treated as a totem of undying.
     * First checks the "c:totems" tag, then falls back to the config file.
     *
     * @param stack The ItemStack to check
     * @return true if the item qualifies as a totem
     */
    public static boolean isTotem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // Primary: check the c:totems item tag
        if (stack.is(ModTags.TOTEMS)) {
            return true;
        }

        // Fallback: check custom config
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return ModConfig.isCustomTotem(itemId);
    }
}
