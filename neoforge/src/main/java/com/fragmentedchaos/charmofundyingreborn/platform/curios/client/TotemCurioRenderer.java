package com.fragmentedchaos.charmofundyingreborn.platform.curios.client;

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
 * 把护符槽里的图腾渲染成身上的小 3D 模型。
 * <p>
 * 位置/缩放/朝向沿用 charmofundying-1.21.x 的数值；绘制走 26.1 的
 * {@link ItemModelResolver} + {@link ItemStackRenderState#submit} 流程，旧的
 * {@code ItemRenderer.renderStatic} 已经不存在了。
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

        // 可调参数：想让图腾换个位置或大小就改这里。
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.2F, -0.15F);
        poseStack.mulPose(Direction.DOWN.getRotation());
        poseStack.scale(0.35F, 0.35F, 0.35F);
        itemState.submit(poseStack, submitNodeCollector, packedLight, OverlayTexture.NO_OVERLAY,
                renderState.outlineColor);
        poseStack.popPose();
    }
}
