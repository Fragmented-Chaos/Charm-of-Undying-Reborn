package com.fragmentedchaos.charmofundyingreborn;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Handles triggering the vanilla totem of undying effect.
 * Instead of re-implementing, this class invokes the same vanilla API calls
 * that the game uses when a totem activates in the player's hand.
 * <p>
 * Vanilla totem behavior (mirrored here):
 * <ol>
 *   <li>Set health to 1.0</li>
 *   <li>Clear all status effects</li>
 *   <li>Apply Regeneration II for 45 seconds</li>
 *   <li>Apply Absorption II for 5 seconds</li>
 *   <li>Apply Fire Resistance I for 40 seconds</li>
 *   <li>Play the totem activation animation (entity event 35)</li>
 * </ol>
 */
public final class TotemEffectHandler {

    // Vanilla constants for the totem effect
    private static final int REGENERATION_DURATION = 900;   // 45 seconds
    private static final int REGENERATION_AMPLIFIER = 1;    // Level II
    private static final int ABSORPTION_DURATION = 100;     // 5 seconds
    private static final int ABSORPTION_AMPLIFIER = 1;      // Level II
    private static final int FIRE_RESISTANCE_DURATION = 800; // 40 seconds
    private static final int FIRE_RESISTANCE_AMPLIFIER = 0; // Level I
    private static final byte TOTEM_ENTITY_EVENT = 35;      // Vanilla totem animation

    private TotemEffectHandler() {
        throw new UnsupportedOperationException("TotemEffectHandler cannot be instantiated");
    }

    /**
     * Activates the totem of undying effect for the given player.
     * This mirrors the exact vanilla behavior that occurs when a totem
     * is consumed from the player's hand.
     *
     * @param player     The player being saved from death
     * @param totemStack The totem item being consumed (for display/logging purposes)
     */
    public static void activateTotem(Player player, ItemStack totemStack) {
        if (player == null || totemStack == null || totemStack.isEmpty()) {
            Constants.LOG.warn("Attempted to activate totem with null/empty parameters");
            return;
        }

        Constants.LOG.debug("Activating totem effect for player {} using {}",
                player.getName().getString(), totemStack.getHoverName().getString());

        // Set health to 1 (half a heart) — vanilla behavior
        player.setHealth(1.0F);

        // Remove all existing status effects — vanilla behavior
        player.removeAllEffects();

        // Apply Regeneration II for 45 seconds (900 ticks) — vanilla behavior
        player.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION, REGENERATION_DURATION, REGENERATION_AMPLIFIER));

        // Apply Absorption II for 5 seconds (100 ticks) — vanilla behavior
        player.addEffect(new MobEffectInstance(
                MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));

        // Apply Fire Resistance I for 40 seconds (800 ticks) — vanilla behavior
        player.addEffect(new MobEffectInstance(
                MobEffects.FIRE_RESISTANCE, FIRE_RESISTANCE_DURATION, FIRE_RESISTANCE_AMPLIFIER));

        // Trigger the totem animation (particles + sound) — vanilla behavior
        player.level().broadcastEntityEvent(player, TOTEM_ENTITY_EVENT);
    }
}
