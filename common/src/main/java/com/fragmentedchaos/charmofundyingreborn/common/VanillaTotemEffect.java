package com.fragmentedchaos.charmofundyingreborn.common;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Default totem effect — mirrors the exact vanilla behavior:
 * set health to 1, clear effects, apply regeneration/absorption/fire-resistance.
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
    public boolean applyEffects(Player player) {
        player.setHealth(1.0F);
        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESIST_DURATION, FIRE_RESIST_AMPLIFIER));
        return true;
    }
}
