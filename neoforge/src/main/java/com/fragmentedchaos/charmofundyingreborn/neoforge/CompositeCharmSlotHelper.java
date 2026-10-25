package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Curios 与 Trinkets 同时存在时合并两者。不隐藏任何一方的槽位，
 * 复活时按顺序取第一个放着图腾的槽，保证一次死亡只消耗一个。
 */
public final class CompositeCharmSlotHelper implements ICharmSlotHelper {

    private final List<ICharmSlotHelper> providers;

    public CompositeCharmSlotHelper(List<ICharmSlotHelper> providers) {
        this.providers = List.copyOf(providers);
    }

    @Override
    public String getPlatformName() {
        StringBuilder names = new StringBuilder();
        for (ICharmSlotHelper provider : this.providers) {
            if (names.length() > 0) {
                names.append('+');
            }
            names.append(provider.getPlatformName());
        }
        return names.toString();
    }

    @Override
    public ItemStack getCharmSlot(Player player) {
        for (ICharmSlotHelper provider : this.providers) {
            ItemStack stack = provider.getCharmSlot(player);
            if (stack != null && !stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasCharmSlot(Player player) {
        for (ICharmSlotHelper provider : this.providers) {
            if (provider.hasCharmSlot(player)) {
                return true;
            }
        }
        return false;
    }
}
