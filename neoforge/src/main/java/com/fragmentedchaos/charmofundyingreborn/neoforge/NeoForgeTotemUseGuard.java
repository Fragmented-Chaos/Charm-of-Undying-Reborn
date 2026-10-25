package com.fragmentedchaos.charmofundyingreborn.neoforge;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * 把 NeoForge 的 LivingUseTotemEvent 补到护符槽上。
 * 护符槽没有手，事件要一个 InteractionHand，用主手代替。
 */
public class NeoForgeTotemUseGuard implements ITotemUseGuard {

    @Override
    public boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource) {
        return CommonHooks.onLivingUseTotem(player, damageSource, stack, InteractionHand.MAIN_HAND);
    }
}
