package com.fragmentedchaos.charmofundyingreborn;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/** 图腾判定用的标签：约定标签 {@code c:totems}。 */
public final class ModTags {

    public static final TagKey<Item> TOTEMS = TagKey.create(
            Registries.ITEM,
            Identifier.parse("c:totems")
    );

    private ModTags() {
        throw new UnsupportedOperationException("ModTags class cannot be instantiated");
    }
}
