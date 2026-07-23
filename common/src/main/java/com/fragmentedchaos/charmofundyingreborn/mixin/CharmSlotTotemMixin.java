package com.fragmentedchaos.charmofundyingreborn.mixin;

import com.fragmentedchaos.charmofundyingreborn.DeathEventHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin that intercepts {@link LivingEntity#checkTotemDeathProtection(DamageSource)}
 * to allow totems in charm accessory slots to trigger the vanilla resurrection.
 * <p>
 * Uses a two-phase injection to match vanilla behavior:
 * <ol>
 *   <li><b>HEAD</b>: Find totem from charm slot and store it (do NOT consume yet)</li>
 *   <li><b>INVOKE Hands.values()</b>: After vanilla's invulnerability check passes,
 *       consume the stored totem and signal success</li>
 * </ol>
 * This ensures that damage types which bypass invulnerability (like /kill and void)
 * are NOT blocked by charm slot totems, matching vanilla hand-held totem behavior.
 */
@Mixin(LivingEntity.class)
public abstract class CharmSlotTotemMixin {

    @Unique
    private boolean charmofundyingreborn$totemFound;

    /**
     * Phase 1: At HEAD, find totem from charm slot but do not consume.
     * Sets {@code totemFound} flag to true if a totem exists.
     */
    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void charmofundyingreborn$precheckTotemDeathProtection(
            DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir) {

        LivingEntity self = (LivingEntity) (Object) this;
        charmofundyingreborn$totemFound = false;

        if (self instanceof Player player && !player.level().isClientSide()) {
            // Phase 1: only check existence — do NOT consume
            charmofundyingreborn$totemFound = DeathEventHandler.hasTotemInCharm(player);
        }
    }

    /**
     * Phase 2: After vanilla passes the BYPASSES_INVULNERABILITY check,
     * consume the previously found totem and signal success.
     */
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

        if (charmofundyingreborn$totemFound && DeathEventHandler.consumeAndActivate((Player) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
