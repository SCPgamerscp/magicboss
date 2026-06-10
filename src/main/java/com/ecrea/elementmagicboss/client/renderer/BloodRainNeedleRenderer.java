package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.BloodRainNeedle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class BloodRainNeedleRenderer extends EntityRenderer<BloodRainNeedle> {

    // irons_spellbooks の血の針テクスチャをそのまま流用
    private static final ResourceLocation TEXTURE =
            IronsSpellbooks.id("textures/entity/blood_needle/needle_5.png");

    public BloodRainNeedleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BloodRainNeedle entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Pose pose = poseStack.last();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y)
                * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x)
                * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                entity.getZRot() + (entity.tickCount + partialTicks) * 40));
        poseStack.mulPose(Axis.XP.rotationDegrees(45));

        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);
        drawSlash(pose, bufferSource, light);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawSlash(Pose pose, MultiBufferSource bufferSource, int light) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        float halfWidth = 2.5f * .5f;
        // 深紅色（BloodNeedle と同じ色）
        consumer.vertex(poseMatrix, 0, -halfWidth, -halfWidth)
                .color(90, 0, 10, 255).uv(0f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, -halfWidth)
                .color(90, 0, 10, 255).uv(1f, 1f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, halfWidth, halfWidth)
                .color(90, 0, 10, 255).uv(1f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normalMatrix, 0f, 1f, 0f).endVertex();
        consumer.vertex(poseMatrix, 0, -halfWidth, halfWidth)
                .color(90, 0, 10, 255).uv(0f, 0f)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normalMatrix, 0f, 1f, 0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BloodRainNeedle entity) {
        return TEXTURE;
    }
}
