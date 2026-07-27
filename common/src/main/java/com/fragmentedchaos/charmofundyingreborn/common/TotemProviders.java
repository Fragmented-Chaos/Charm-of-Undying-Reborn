package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.ModTags;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of {@link ITotemEffect} providers keyed by item registry name.
 * <p>
 * Items tagged with {@code c:totems} automatically resolve to the
 * {@link #VANILLA} effect without needing explicit registration.
 * Custom effects can be registered via {@link #register(String, ITotemEffect)}.
 */
public final class TotemProviders {

    public static final ITotemEffect VANILLA = new VanillaTotemEffect();

    private static final Map<String, ITotemEffect> PROVIDERS = new ConcurrentHashMap<>();

    private TotemProviders() {}

    /** Called once during mod init. */
    public static void init() {
        PROVIDERS.put("minecraft:totem_of_undying", VANILLA);
    }

    /**
     * Registers a custom effect for the given item ID.
     * @param itemId e.g. "modid:custom_totem"
     */
    public static void register(String itemId, ITotemEffect effect) {
        PROVIDERS.put(itemId, effect);
    }

    /** Looks up the effect provider for an item. Falls back to VANILLA for tagged items. */
    public static Optional<ITotemEffect> getEffect(Item item) {
        String id = BuiltInRegistries.ITEM.getKey(item).toString();
        ITotemEffect provider = PROVIDERS.get(id);
        if (provider != null) return Optional.of(provider);
        // Tag-based fallback: any c:totems-tagged item gets vanilla effect
        if (item.builtInRegistryHolder().is(ModTags.TOTEMS)) {
            return Optional.of(VANILLA);
        }
        return Optional.empty();
    }
}
