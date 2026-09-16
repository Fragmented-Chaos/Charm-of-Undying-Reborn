package com.fragmentedchaos.charmofundyingreborn.platform;

import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * NeoForge implementation of {@link ITotemUseGuard}.
 * <p>
 * Fires NeoForge's {@code LivingUseTotemEvent} for a charm-slot totem, so mods that veto totem use
 * (disabled dimensions, boss fights, hardcore rules, ...) apply here exactly as they do for a
 * hand-held totem.
 * <p>
 * The event carries an {@link InteractionHand}; a charm slot has no hand, so the main hand is the
 * closest available stand-in.
 */
public class NeoForgeTotemUseGuard implements ITotemUseGuard {

    @Override
    public boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource) {
        return CommonHooks.onLivingUseTotem(player, damageSource, stack, InteractionHand.MAIN_HAND);
    }
}
