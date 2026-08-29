package com.fragmentedchaos.charmofundyingreborn.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Renders a Totem of Undying (any item in the {@code c:totems} tag) as a small 3D model on the
 * player's body when it is equipped in a Curios "charm" slot.
 *
 * <p>Placement is ported from the reference mod {@code charmofundying-1.21.x}
 * ({@code client/TotemRenderer} + {@code CurioTotemRenderer}): translate to the body
 * ({@code 0, 0.2, -0.15}), uniform scale {@code 0.35}, orient with
 * {@link Direction#DOWN}, and render with {@link ItemDisplayContext#NONE}.
 *
 * <p>The actual model draw uses the Minecraft 26.1 item pipeline
 * ({@link ItemModelResolver} + {@link ItemStackRenderState#submit}) because the classic
 * {@code ItemRenderer.renderStatic(...)} API no longer exists in this version.
 */
public class TotemCurioRenderer implements ICurioRenderer {

    @Override
    public <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int packedLight,
            S renderState,
            RenderLayerParent<S, M> renderLayerParent,
            EntityRendererProvider.Context context,
            float yRotation,
            float xRotation) {

        if (stack.isEmpty()) {
            return;
        }

        // Resolve the item's baked 3D model into a render state using the new item pipeline.
        ItemModelResolver resolver = context.getItemModelResolver();
        ItemStackRenderState itemState = new ItemStackRenderState();
        resolver.updateForTopItem(
                itemState,
                stack,
                ItemDisplayContext.NONE,
                Minecraft.getInstance().level,
                null,
                0);

        if (itemState.isEmpty()) {
            return;
        }

        // Placement ported from charmofundying-1.21.x: a small charm on the body, oriented down.
        // NOTE: visual/tunable values; adjust to taste for a specific totem model.
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.2F, -0.15F);
        poseStack.mulPose(Direction.DOWN.getRotation());
        poseStack.scale(0.35F, 0.35F, 0.35F);
        itemState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY,
                renderState.outlineColor);
        poseStack.popPose();
    }
}
