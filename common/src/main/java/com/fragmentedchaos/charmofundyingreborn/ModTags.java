package com.fragmentedchaos.charmofundyingreborn;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Central tag definitions for identifying totems of undying.
 * Uses the Convention tag "c:totems" as the primary compatibility mechanism.
 */
public final class ModTags {

    /**
     * Convention item tag for totems of undying.
     * Any item in this tag will be treated as a valid totem by the mod.
     */
    public static final TagKey<Item> TOTEMS = TagKey.create(
            Registries.ITEM,
            Identifier.parse("c:totems")
    );

    private ModTags() {
        throw new UnsupportedOperationException("ModTags class cannot be instantiated");
    }
}
