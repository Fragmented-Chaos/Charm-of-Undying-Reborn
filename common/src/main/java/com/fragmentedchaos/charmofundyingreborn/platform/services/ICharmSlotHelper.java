package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/** 平台无关的护符槽访问：Fabric 用 Trinkets，NeoForge 用 Curios 或 Trinkets。 */
public interface ICharmSlotHelper {

    String getPlatformName();

    /** 槽位里的物品；没有槽位或槽位为空时返回 EMPTY。 */
    @Nullable
    ItemStack getCharmSlot(Player player);

    boolean hasCharmSlot(Player player);

    /**
     * 修改槽位里的物品并写回。
     * 必须写回是因为 Curios 17 每次读取都重建 ItemStack，直接改返回值落不进背包。
     */
    void modifyCharmSlot(Player player, Consumer<ItemStack> action);
}
