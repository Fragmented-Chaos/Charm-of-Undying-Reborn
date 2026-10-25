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
 * 给所有带 {@code c:totems} 标签的物品注册动态 {@link ICurioItem}，让图腾能右键直接装进槽位。
 * Trinkets 不需要这一步，它的槽位与标签都是数据文件。
 */
public final class CuriosTotemRegistration {

    private static final ICurioItem TOTEM_CURIO_ITEM = new TotemCurioItem();
    private static final Set<Item> REGISTERED_CURIO_ITEMS = new HashSet<>();

    private CuriosTotemRegistration() {
    }

    /** 幂等：每个物品只注册一次。 */
    public static void registerTotemCurios() {
        for (Item item : BuiltInRegistries.ITEM) {
            if (TotemHelper.isTotem(item.getDefaultInstance()) && REGISTERED_CURIO_ITEMS.add(item)) {
                CuriosApi.registerCurio(item, TOTEM_CURIO_ITEM);
                Constants.LOG.debug("Registered dynamic ICurioItem for totem item {}",
                        BuiltInRegistries.ITEM.getKey(item));
            }
        }
    }

    /** canEquipFromUse 恒为 true：Curios 17.0.0-beta 在目标槽已占用时会漏 root transaction（上游 bug）。 */
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
