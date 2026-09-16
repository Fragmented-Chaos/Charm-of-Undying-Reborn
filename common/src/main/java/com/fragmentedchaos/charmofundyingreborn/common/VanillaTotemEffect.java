package com.fragmentedchaos.charmofundyingreborn.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;

/**
 * Default totem effect.
 * <p>
 * Prefers the item's own {@link DeathProtection} data component — the mechanism vanilla has used
 * since 26.1 — so that every {@code c:totems} item describes its own resurrection. This is what
 * makes custom totems (with their own {@code death_effects}) behave correctly instead of silently
 * receiving the vanilla totem's effects.
 * <p>
 * Falls back to the vanilla totem's effect list for tagged items that carry no such component.
 */
public class VanillaTotemEffect implements ITotemEffect {

    private static final int REGEN_DURATION = 900;
    private static final int REGEN_AMPLIFIER = 1;
    private static final int ABSORPTION_DURATION = 100;
    private static final int ABSORPTION_AMPLIFIER = 1;
    private static final int FIRE_RESIST_DURATION = 800;
    private static final int FIRE_RESIST_AMPLIFIER = 0;

    @Override
    public void modifyStack(ItemStack stack) {
        stack.shrink(1);
    }

    @Override
    public boolean applyEffects(Player player, ItemStack stack) {
        DeathProtection protection = stack.get(DataComponents.DEATH_PROTECTION);
        if (protection != null) {
            // Let the item run its own death_effects, exactly as vanilla does.
            protection.applyEffects(stack, player);
            return true;
        }

        // Fallback for items tagged c:totems that carry no DEATH_PROTECTION component.
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESIST_DURATION, FIRE_RESIST_AMPLIFIER));
        return true;
    }
}
