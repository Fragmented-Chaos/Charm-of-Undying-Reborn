package com.fragmentedchaos.charmofundyingreborn.mixin;

import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

/**
 * NeoForge-only: limits the Curios charm slot to a single item.
 * <p>
 * When a stackable item (e.g. 64 dirt) is dropped into the charm slot,
 * only one is inserted and the remainder is returned to the player.
 * <p>
 * Injects into the parent {@link ItemStackHandler#getSlotLimit(int)} because
 * {@code DynamicStackHandler} only inherits it. Only applies when the target
 * instance is Curios' DynamicStackHandler for the "charm" slot.
 */
@Mixin(ItemStackHandler.class)
public abstract class CharmSlotLimitMixin {

    @Inject(method = "getSlotLimit", at = @At("HEAD"), cancellable = true)
    private void charmofundyingreborn$limitCharmSlot(int slot, CallbackInfoReturnable<Integer> cir) {
        if (!this.getClass().getName().equals("top.theillusivec4.curios.common.inventory.DynamicStackHandler")) {
            return;
        }
        try {
            java.lang.reflect.Field field = this.getClass().getDeclaredField("ctxBuilder");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Function<Object, Object> fn = (Function<Object, Object>) field.get(this);
            Object ctx = fn.apply(slot);
            String id = (String) ctx.getClass().getMethod("identifier").invoke(ctx);
            if ("charm".equals(id)) {
                cir.setReturnValue(1);
            }
        } catch (Exception ignored) {
            // Fall back to default slot limit on any reflection issue
        }
    }
}
