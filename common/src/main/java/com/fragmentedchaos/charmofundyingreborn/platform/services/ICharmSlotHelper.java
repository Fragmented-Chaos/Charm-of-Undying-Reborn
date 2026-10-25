package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

/** 平台无关的护符槽访问：Fabric 用 Trinkets，NeoForge 用 Curios。 */
public interface ICharmSlotHelper {

    String getPlatformName();

    /** 槽位里的物品；没有槽位或槽位为空时返回 EMPTY。 */
    @Nullable
    ItemStack getCharmSlot(Player player);

    boolean hasCharmSlot(Player player);
}
