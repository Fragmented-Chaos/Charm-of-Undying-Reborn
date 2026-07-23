package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/**
 * NeoForge implementation of ICharmSlotHelper using Curios API.
 * Uses findFirstCurio() to search all curio slots for totem items.
 */
public class NeoForgeCharmSlotHelper implements ICharmSlotHelper {

    @Override
    public String getPlatformName() {
        return "Curios";
    }

    @Override
    public ItemStack getCharmSlot(Player player) {
        return CuriosApi.getCuriosInventory(player).map(
                inv -> inv.findFirstCurio(stack -> TotemHelper.isTotem(stack))
                        .map(SlotResult::stack)
                        .orElse(ItemStack.EMPTY)
        ).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean hasCharmSlot(Player player) {
        return CuriosApi.getCuriosInventory(player).isPresent();
    }
}
