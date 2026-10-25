package com.fragmentedchaos.charmofundyingreborn.platform.curios;

import com.fragmentedchaos.charmofundyingreborn.common.TotemHelper;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ICharmSlotHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Consumer;

/** Curios 实现：在所有 curio 槽里找图腾。只在 Curios 存在时实例化。 */
public class CuriosCharmSlotHelper implements ICharmSlotHelper {

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

    @Override
    public void modifyCharmSlot(Player player, Consumer<ItemStack> action) {
        CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                inv.findFirstCurio(TotemHelper::isTotem).ifPresent(result -> {
                    // Curios 17 每次读取都重建 ItemStack，改完必须写回。
                    ItemStack stack = result.stack();
                    action.accept(stack);
                    SlotContext context = result.slotContext();
                    inv.setEquippedCurio(context.identifier(), context.index(), stack);
                }));
    }
}
