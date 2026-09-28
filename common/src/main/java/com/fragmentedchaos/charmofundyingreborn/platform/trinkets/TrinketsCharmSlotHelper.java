package com.fragmentedchaos.charmofundyingreborn.platform.trinkets;

import com.fragmentedchaos.charmofundyingreborn.Constants;
import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Trinkets Updated implementation of {@link ICharmSlotHelper}.
 * <p>
 * Lives in {@code common} because Trinkets is a multiloader mod: the very same API (backed by Yumi)
 * is available on Fabric and on NeoForge, so both loaders share this implementation. It searches
 * every trinket inventory for a totem, which is how the Fabric side has always worked.
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

    @Override
    public void modifyCharmSlot(Player player, Consumer<ItemStack> action) {
        try {
            for (TrinketInventory inv : TrinketsApi.getAttachment(player).getInventories().values()) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (TotemHelper.isTotem(stack)) {
                        action.accept(stack);
                        // Trinkets hands out the live stack, but write it back anyway so this keeps
                        // working if it ever switches to snapshots the way Curios 17 did.
                        inv.setItem(i, stack);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.warn("Error modifying Trinkets charm slot for player {}: {}",
                    player.getName().getString(), e.getMessage());
        }
    }
}
