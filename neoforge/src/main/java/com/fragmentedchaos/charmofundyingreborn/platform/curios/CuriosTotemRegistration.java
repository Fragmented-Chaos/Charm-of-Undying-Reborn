package com.fragmentedchaos.charmofundyingreborn.platform.curios;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashSet;
import java.util.Set;

/**
 * Registers a dynamic {@link ICurioItem} on every item that carries the {@code c:totems} tag, so
 * totems can be auto-equipped from the hotbar (right-click). Only totem items are registered, so
 * other mods' items are never intercepted or overridden.
 * <p>
 * Only touched when Curios is loaded (curios is optional now that Trinkets Updated can provide the
 * slot too). Trinkets needs no registration: its slots are declared by data files shipped in
 * {@code common/src/main/resources/data/trinkets}.
 * <p>
 * Note: {@code canEquipFromUse} deliberately returns true. Curios 17.0.0-beta leaks a root
 * transaction in that code path (CuriosCommonEvents#curioRightClick line 372) when the target slot
 * is occupied; that is a Curios bug, tracked upstream.
 */
public final class CuriosTotemRegistration {

    private static final ICurioItem TOTEM_CURIO_ITEM = new TotemCurioItem();
    private static final Set<Item> REGISTERED_CURIO_ITEMS = new HashSet<>();

    private CuriosTotemRegistration() {
    }

    /** Idempotent: each item is registered at most once. */
    public static void registerTotemCurios() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (TotemHelper.isTotem(item.getDefaultInstance()) && REGISTERED_CURIO_ITEMS.add(item)) {
                CuriosApi.registerCurio(item, TOTEM_CURIO_ITEM);
                Constants.LOG.debug("Registered dynamic ICurioItem for totem item {}",
                        BuiltInRegistries.ITEM.getKey(item));
            }
        }
    }

    /**
     * Dynamic {@link ICurioItem}: only totem stacks (tag OR config) report a curio capability.
     * Evaluated at runtime so config reloads take effect immediately.
     */
    private static class TotemCurioItem implements ICurioItem {

        @Override
        public boolean hasCurioCapability(ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }

        @Override
        public boolean canEquip(SlotContext slotContext, ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }

        @Override
        public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
            return TotemHelper.isTotem(stack);
        }
    }
}
