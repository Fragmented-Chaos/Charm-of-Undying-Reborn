package com.fragmentedchaos.charmofundyingreborn.platform.services;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Platform hook that lets other mods veto a charm-slot resurrection.
 * <p>
 * NeoForge fires {@code LivingUseTotemEvent} from inside vanilla's {@code InteractionHand} loop, so
 * it only covers hand-held totems. This mod short-circuits that loop before it runs, which would
 * otherwise let players bypass every mod that cancels the event. Implementing this service restores
 * the same veto for the charm slot.
 * <p>
 * Fabric has no equivalent event, so its implementation always allows the use.
 */
public interface ITotemUseGuard {

    /**
     * @param player       the player about to be saved
     * @param stack        the totem stack found in the charm slot
     * @param damageSource the damage that would have killed the player
     * @return true if the resurrection may proceed
     */
    boolean allowTotemUse(Player player, ItemStack stack, DamageSource damageSource);
}
