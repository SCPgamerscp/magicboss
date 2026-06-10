package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.MagicImpactCrossEntity;
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

public class MagicImpactCrossRenderer extends EntityRenderer<MagicImpactCrossEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/beacon_beam.png");

    public MagicImpactCrossRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(MagicImpactCrossEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(entity.getAgeScale(partialTick), entity.getAgeScale(partialTick), entity.getAgeScale(partialTick));
        renderCross(entity, poseStack, bufferSource, 255, 244, 210, 210);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    static void renderCrossBars(PoseStack poseStack, MultiBufferSource bufferSource,
                                float verticalWidth, float verticalHeight,
                                float horizontalWidth, float horizontalHeight,
                                int red, int green, int blue, int alpha) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            drawBar(poseStack.last(), consumer, verticalWidth, verticalHeight, verticalHeight * 0.5f,
                    red, green, blue, alpha);
            drawBar(poseStack.last(), consumer, horizontalWidth, horizontalHeight, verticalHeight * 0.62f,
                    red, green, blue, alpha);
            poseStack.popPose();
        }
    }

    private void renderCross(MagicImpactCrossEntity entity, PoseStack poseStack,
                             MultiBufferSource bufferSource, int red, int green, int blue, int alpha) {
        renderCrossBars(
                poseStack,
                bufferSource,
                6.0f,
                MagicImpactCrossEntity.CROSS_HEIGHT,
                MagicImpactCrossEntity.CROSS_WIDTH,
                5.0f,
                red, green, blue, alpha
        );
        renderCrossBars(
                poseStack,
                bufferSource,
                3.0f,
                MagicImpactCrossEntity.CROSS_HEIGHT * 0.92f,
                MagicImpactCrossEntity.CROSS_WIDTH * 0.82f,
                2.5f,
                255, 255, 255, 255
        );
    }

    private static void drawBar(Pose pose, VertexConsumer consumer, float width, float height, float centerY,
                                int red, int green, int blue, int alpha) {
        float halfWidth = width * 0.5f;
        float minY = centerY - height * 0.5f;
        float maxY = centerY + height * 0.5f;
        vertex(pose.pose(), pose.normal(), consumer, -halfWidth, minY, 0.0f, 0.0f, 1.0f, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, halfWidth, minY, 0.0f, 1.0f, 1.0f, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, halfWidth, maxY, 0.0f, 1.0f, 0.0f, red, green, blue, alpha);
        vertex(pose.pose(), pose.normal(), consumer, -halfWidth, maxY, 0.0f, 0.0f, 0.0f, red, green, blue, alpha);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v,
                               int red, int green, int blue, int alpha) {
        builder.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MagicImpactCrossEntity entity) {
        return TEXTURE;
    }
}
