package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.ModTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 按物品注册名索引的 {@link ITotemEffect} 表；带 {@code c:totems} 标签的物品自动用原版效果。 */
public final class TotemProviders {

    public static final ITotemEffect VANILLA = new VanillaTotemEffect();

    private static final Map<String, ITotemEffect> PROVIDERS = new ConcurrentHashMap<>();

    private TotemProviders() {}

    public static void init() {
        PROVIDERS.put("minecraft:totem_of_undying", VANILLA);
    }

    /** @param itemId 形如 {@code modid:custom_totem} */
    public static void register(String itemId, ITotemEffect effect) {
        PROVIDERS.put(itemId, effect);
    }

    public static Optional<ITotemEffect> getEffect(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        ITotemEffect provider = PROVIDERS.get(id);
        if (provider != null) return Optional.of(provider);
        if (item.builtInRegistryHolder().is(ModTags.TOTEMS)) {
            return Optional.of(VANILLA);
        }
        return Optional.empty();
    }
}
