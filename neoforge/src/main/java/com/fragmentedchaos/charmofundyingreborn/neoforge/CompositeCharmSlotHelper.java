package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Combines several accessory providers, for instances that have Curios and Trinkets Updated
 * installed side by side.
 * <p>
 * Nothing is hidden or disabled: both mods keep rendering and managing their own slots. For the
 * resurrection itself the providers are queried in order and the <b>first slot that actually holds
 * a totem</b> wins, so a single death still consumes exactly one totem no matter how many charm
 * slots exist.
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
