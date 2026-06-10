package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.CrimsonYoungMoonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CrimsonYoungMoonRenderer extends EntityRenderer<CrimsonYoungMoonEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/crimson_young_moon.png");

    public CrimsonYoungMoonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(CrimsonYoungMoonEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        float scale = 6.5f * entity.getPulseScale(partialTick);
        poseStack.scale(scale, scale, scale);

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(TEXTURE));

        vertex(poseMatrix, normalMatrix, consumer, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f);
        vertex(poseMatrix, normalMatrix, consumer, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v) {
        builder.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(CrimsonYoungMoonEntity entity) {
        return TEXTURE;
    }
}
