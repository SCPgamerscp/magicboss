package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.DanmakuShotProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DanmakuShotProjectileRenderer extends EntityRenderer<DanmakuShotProjectile> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/danmaku_shot_projectile.png");
    private static final int FRAME_COUNT = 13;

    public DanmakuShotProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DanmakuShotProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.scale(1.10F, 1.10F, 1.10F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderSprite(poseStack, bufferSource, entity.getVariant());
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private static void renderSprite(PoseStack poseStack, MultiBufferSource bufferSource, int variant) {
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(TEXTURE));

        float minU = variant / (float) FRAME_COUNT;
        float maxU = (variant + 1) / (float) FRAME_COUNT;

        vertex(poseMatrix, normalMatrix, consumer, -0.5F, -0.5F, 0.0F, minU, 1.0F, LightTexture.FULL_BRIGHT);
        vertex(poseMatrix, normalMatrix, consumer, 0.5F, -0.5F, 0.0F, maxU, 1.0F, LightTexture.FULL_BRIGHT);
        vertex(poseMatrix, normalMatrix, consumer, 0.5F, 0.5F, 0.0F, maxU, 0.0F, LightTexture.FULL_BRIGHT);
        vertex(poseMatrix, normalMatrix, consumer, -0.5F, 0.5F, 0.0F, minU, 0.0F, LightTexture.FULL_BRIGHT);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v, int packedLight) {
        builder.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normals, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DanmakuShotProjectile entity) {
        return TEXTURE;
    }
}