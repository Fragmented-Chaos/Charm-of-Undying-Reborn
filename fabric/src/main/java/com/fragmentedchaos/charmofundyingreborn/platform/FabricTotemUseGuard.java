package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric implementation of {@link ITotemUseGuard}.
 * <p>
 * Fabric has no totem-use event to mirror, and Trinkets already routes its own slot lookups through
 * the vanilla code path, so every charm-slot resurrection is allowed.
 */
public class FabricTotemUseGuard implements ITotemUseGuard {

    @Override
    public boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource) {
        return true;
    }
}
