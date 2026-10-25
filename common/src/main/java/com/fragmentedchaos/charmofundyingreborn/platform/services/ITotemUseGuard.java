package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 让其它模组能否决护符槽复活。
 * NeoForge 的 LivingUseTotemEvent 只在原版手牌循环里触发，而本模组会短路那个循环，所以这里补一次。
 * Fabric 没有对应事件，实现恒为 true。
 */
public interface ITotemUseGuard {

    /** @return true 表示允许这次复活 */
    boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource);
}
