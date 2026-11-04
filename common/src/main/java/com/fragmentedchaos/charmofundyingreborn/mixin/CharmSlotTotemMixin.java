package com.fragmentedchaos.charmofundyingreborn.mixin;

import com.fragmentedchaos.charmofundyingreborn.common.DeathEventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让护符槽里的图腾参与 {@code LivingEntity#checkTotemDeathProtection}。
 * <p>
 * 注入在 {@code InteractionHand.values()} 之前：该位置在原版无敌检查之后，所以 /kill 与虚空照旧无视护符槽图腾；
 * 同时它也在原版手牌循环之前，所以护符槽会比手持图腾先被消耗。
 */
@Mixin(LivingEntity.class)
public abstract class CharmSlotTotemMixin {

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/world/InteractionHand.values()[Lnet/minecraft/world/InteractionHand;"
            ),
            cancellable = true
    )
    private void charmofundyingreborn$checkTotemDeathProtection(
            DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir) {

        if ((Object) this instanceof Player player
                && !player.level().isClientSide()
                && DeathEventHandler.consumeAndActivate(player, damageSource)) {
            cir.setReturnValue(true);
        }
    }
}
