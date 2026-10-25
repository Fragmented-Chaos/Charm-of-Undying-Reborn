package com.fragmentedchaos.charmofundyingreborn.fabric;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Fabric 没有对应的否决事件，恒放行。 */
public class FabricTotemUseGuard implements ITotemUseGuard {

    @Override
    public boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource) {
        return true;
    }
}
