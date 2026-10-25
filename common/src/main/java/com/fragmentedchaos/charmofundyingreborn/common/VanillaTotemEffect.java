package com.fragmentedchaos.charmofundyingreborn.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;

/**
 * 默认图腾效果。
 * <p>
 * 优先用物品自带的 {@link DeathProtection} 数据组件，这样自定义图腾（自带 death_effects）会按自己的配置生效；
 * 没有该组件的 {@code c:totems} 物品才退回原版图腾那套效果。
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
            protection.applyEffects(stack, player);
            return true;
        }

        player.removeAllEffects();
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_DURATION, REGEN_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESIST_DURATION, FIRE_RESIST_AMPLIFIER));
        return true;
    }
}
