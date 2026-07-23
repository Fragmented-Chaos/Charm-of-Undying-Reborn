package com.fragmentedchaos.charmofundyingreborn;

import com.fragmentedchaos.charmofundyingreborn.platform.CharmSlotServices;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Shared death event handling logic.
 * Called by platform-specific event listeners when a player is about to die.
 * <p>
 * Logic:
 * <ol>
 *   <li>Retrieve the item from the player's charm slot</li>
 *   <li>Check if the item qualifies as a totem (tag + config)</li>
 *   <li>If yes: consume the totem from the charm slot, trigger the vanilla
 *       totem effect, and return {@code true} to signal the death was prevented</li>
 *   <li>If no: return {@code false} — normal death proceeds</li>
 * </ol>
 */
public final class DeathEventHandler {

    private DeathEventHandler() {
        throw new UnsupportedOperationException("DeathEventHandler cannot be instantiated");
    }

    /**
     * Checks if the player has a totem in any curio slot (without consuming it).
     * Used in Phase 1 of the Mixin (at HEAD of checkTotemDeathProtection).
     *
     * @param player The player who is about to die
     * @return true if a totem exists in a curio slot
     */
    public static boolean hasTotemInCharm(Player player) {
        if (player == null || player.isRemoved()) {
            return false;
        }
        try {
            if (!CharmSlotServices.CHARM_SLOT.hasCharmSlot(player)) {
                return false;
            }
            ItemStack charmStack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            return charmStack != null && !charmStack.isEmpty() && TotemHelper.isTotem(charmStack);
        } catch (Exception e) {
            Constants.LOG.error("Error checking charm slot for player {}: {}",
                    player.getName().getString(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * Consumes a totem from the player's curio slot and triggers the vanilla effect.
     * Used in Phase 2 of the Mixin (after vanilla's invulnerability check passes).
     *
     * @param player The player who is about to die
     * @return true if a totem was consumed and activated
     */
    public static boolean consumeAndActivate(Player player) {
        if (player == null || player.isRemoved()) {
            return false;
        }
        try {
            ItemStack charmStack = CharmSlotServices.CHARM_SLOT.getCharmSlot(player);
            if (charmStack == null || charmStack.isEmpty() || !TotemHelper.isTotem(charmStack)) {
                return false;
            }
            ItemStack totemCopy = charmStack.copy();
            charmStack.shrink(1);
            TotemEffectHandler.activateTotem(player, totemCopy);
            Constants.LOG.info("Totem from charm slot activated for player {}!",
                    player.getName().getString());
            return true;
        } catch (Exception e) {
            Constants.LOG.error("Error consuming totem for player {}: {}",
                    player.getName().getString(), e.getMessage(), e);
            return false;
        }
    }
}
