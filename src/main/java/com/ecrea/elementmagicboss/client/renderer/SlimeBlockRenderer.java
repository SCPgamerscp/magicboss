package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.SlimeBlockProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

public class SlimeBlockRenderer extends EntityRenderer<SlimeBlockProjectile> {

    private final BlockRenderDispatcher blockRenderer;

    public SlimeBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(SlimeBlockProjectile entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        float scale = Math.max(0.1f, entity.getBbWidth());
        poseStack.scale(scale, scale, scale);
        // Spin animation
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.tickCount * 8f));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.tickCount * 5f));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        blockRenderer.renderSingleBlock(
                Blocks.SLIME_BLOCK.defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeBlockProjectile entity) {
        return new ResourceLocation("minecraft", "textures/block/slime_block.png");
    }
}
