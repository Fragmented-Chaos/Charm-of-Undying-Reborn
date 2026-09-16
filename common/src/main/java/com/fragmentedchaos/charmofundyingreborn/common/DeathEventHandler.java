package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import com.fragmentedchaos.charmofundyingreborn.platform.services.ITotemUseGuard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Optional;

/**
 * Shared death handling. Phase 1 checks existence; Phase 2 consumes and activates
 * using an optional {@link ITotemEffect} provider for custom totem behavior.
 */
public final class DeathEventHandler {

    private DeathEventHandler() {
        throw new UnsupportedOperationException("DeathEventHandler cannot be instantiated");
    }

    public static boolean hasTotemInCharm(Player player) {
        if (player == null || player.isRemoved()) return false;
        try {
            if (!CharmSlotServices.CHARM_SLOT.hasCharmSlot(player)) return false;
            ItemStack stack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            return stack != null && !stack.isEmpty() && TotemHelper.isTotem(stack);
        } catch (Exception e) {
            Constants.LOG.error("Error checking charm slot: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Consume and activate the totem in the charm slot.
     * Looks up an {@link ITotemEffect} provider for the item;
     * falls back to vanilla behavior if none registered.
     *
     * @param damageSource the damage that would have killed the player, forwarded to the
     *                     platform totem-use guard so other mods can veto the resurrection
     */
    public static boolean consumeAndActivate(Player player, DamageSource damageSource) {
        if (player == null || player.isRemoved()) return false;
        try {
            ItemStack stack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            if (stack == null || stack.isEmpty() || !TotemHelper.isTotem(stack)) return false;

            // Give other mods the same veto they get for a hand-held totem. Vanilla only offers it
            // inside its InteractionHand loop, which this mixin short-circuits before reaching.
            ITotemUseGuard guard = CharmSlotServices.TOTEM_GUARD;
            if (guard != null && !guard.allowTotemUse(player, stack, damageSource)) return false;

            Optional<ITotemEffect> provider = TotemProviders.getEffect(stack.getItem());
            ITotemEffect effect = provider.orElse(TotemProviders.VANILLA);

            ItemStack copy = stack.copy();
            effect.modifyStack(stack);
            // Vanilla sets health before running the item's death effects.
            player.setHealth(1.0F);
            boolean ok = effect.applyEffects(player, copy);
            if (ok) {
                player.level().broadcastEntityEvent(player, (byte) 35);
                if (player instanceof ServerPlayer sp) {
                    // Vanilla emits this so sculk sensors / wardens notice the totem being used.
                    copy.causeUseVibration(player, GameEvent.ITEM_INTERACT_FINISH);
                    CharmSlotServices.NETWORK.sendTotemUse(sp, copy);
                    // Match vanilla totem behavior: award the "item used" stat and fire the
                    // "Totem of Undying" advancement (used_totem).
                    sp.awardStat(Stats.ITEM_USED.get(copy.getItem()), 1);
                    AdvancementCompat.triggerUsedTotem(sp, copy);
                }
            }
            return ok;
        } catch (Exception e) {
            Constants.LOG.error("Error consuming totem", e);
            return false;
        }
    }
}
