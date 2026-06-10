package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.ApostleBeamCrossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 使徒光線の光の十字架レンダラー。
 * MagicImpactCrossRendererと同じ描画手法を使用し、色味を黄金色にする。
 */
public class ApostleBeamCrossRenderer extends EntityRenderer<ApostleBeamCrossEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/beacon_beam.png");

    public ApostleBeamCrossRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(ApostleBeamCrossEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getAgeScale(partialTick);
        poseStack.scale(scale, scale, scale);
        // 外側: 黄金色の光
        renderCross(poseStack, bufferSource, 255, 230, 140, 200);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderCross(PoseStack poseStack, MultiBufferSource bufferSource,
                             int red, int green, int blue, int alpha) {
        // 外側の十字架
        MagicImpactCrossRenderer.renderCrossBars(
                poseStack,
                bufferSource,
                4.0f,
                ApostleBeamCrossEntity.CROSS_HEIGHT,
                ApostleBeamCrossEntity.CROSS_WIDTH,
                3.5f,
                red, green, blue, alpha
        );
        // 内側の白い芯
        MagicImpactCrossRenderer.renderCrossBars(
                poseStack,
                bufferSource,
                2.0f,
                ApostleBeamCrossEntity.CROSS_HEIGHT * 0.92f,
                ApostleBeamCrossEntity.CROSS_WIDTH * 0.82f,
                1.8f,
                255, 255, 255, 255
        );
    }

    @Override
    public ResourceLocation getTextureLocation(ApostleBeamCrossEntity entity) {
        return TEXTURE;
    }
}
