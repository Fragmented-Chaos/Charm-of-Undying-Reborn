package com.fragmentedchaos.charmofundyingreborn.common;

import com.fragmentedchaos.charmofundyingreborn.Constants;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

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
     */
    public static boolean consumeAndActivate(Player player) {
        if (player == null || player.isRemoved()) return false;
        try {
            ItemStack stack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            if (stack == null || stack.isEmpty() || !TotemHelper.isTotem(stack)) return false;

            Optional<ITotemEffect> provider = TotemProviders.getEffect(stack.getItem());
            ITotemEffect effect = provider.orElse(TotemProviders.VANILLA);

            ItemStack copy = stack.copy();
            effect.modifyStack(stack);
            boolean ok = effect.applyEffects(player);
            if (ok) {
                player.level().broadcastEntityEvent(player, (byte) 35);
                if (player instanceof ServerPlayer sp) {
                    CharmSlotServices.NETWORK.sendTotemUse(sp, copy);
                }
                Constants.LOG.info("Totem from charm slot activated for player {}!", player.getName().getString());
            }
            return ok;
        } catch (Exception e) {
            Constants.LOG.error("Error consuming totem", e);
            return false;
        }
    }
}
