package com.fragmentedchaos.charmofundyingreborn.platform.trinkets;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Trinkets 实现。放在 common 是因为 Trinkets 是多加载器模组，两端共用同一套 API。
 * 查找范围是全部 trinket 背包。
 */
public class TrinketsCharmSlotHelper implements ICharmSlotHelper {

    @Override
    public String getPlatformName() {
        return "Trinkets";
    }

    @Override
    public ItemStack getCharmSlot(Player player) {
        try {
            for (TrinketInventory inv : TrinketsApi.getAttachment(player).getInventories().values()) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (TotemHelper.isTotem(stack)) {
                        return stack;
                    }
                }
            }
            return ItemStack.EMPTY;
        } catch (Exception e) {
            Constants.LOG.warn("Error accessing Trinkets slots for player {}: {}",
                    player.getName().getString(), e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean hasCharmSlot(Player player) {
        try {
            return !TrinketsApi.getAttachment(player).getInventories().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
