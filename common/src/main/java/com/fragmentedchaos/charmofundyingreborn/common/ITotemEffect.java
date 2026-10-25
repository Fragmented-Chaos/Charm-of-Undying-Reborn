package com.fragmentedchaos.charmofundyingreborn.common;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 自定义图腾效果。用 {@link TotemProviders#register} 注册；带 {@code c:totems} 标签的物品默认走原版效果。 */
public interface ITotemEffect {

    /** 预留接口，当前未生效：注入点在原版无敌检查之后，护符槽图腾不能绕过 /kill 与虚空。 */
    default boolean bypassInvul() {
        return false;
    }

    /** 消耗图腾，默认减 1。只在 {@link #applyEffects} 成功后被调用。 */
    void modifyStack(ItemStack stack);

    /** 施加复活效果，调用前生命值已被设为 1。返回 false 表示未生效，图腾不消耗。 */
    boolean applyEffects(Player player, ItemStack stack);
}
