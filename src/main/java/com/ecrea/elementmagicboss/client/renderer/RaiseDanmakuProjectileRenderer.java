package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.RaiseDanmakuProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
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

public class RaiseDanmakuProjectileRenderer extends EntityRenderer<RaiseDanmakuProjectile> {

    // DanmakuShotと同じテクスチャシートを流用（形状は同じ）
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/danmaku_shot_projectile.png");
    private static final int FRAME_COUNT = 13;

    public RaiseDanmakuProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RaiseDanmakuProjectile entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.scale(1.05F, 1.05F, 1.05F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderSprite(poseStack, bufferSource, entity.getVariant());
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    /** 炎のオレンジ色でレンダリング */
    private static void renderSprite(PoseStack poseStack, MultiBufferSource bufferSource, int variant) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(TEXTURE));

        float minU = variant / (float) FRAME_COUNT;
        float maxU = (variant + 1) / (float) FRAME_COUNT;

        // オレンジ色のティント (R=255, G=140, B=30)
        vertex(poseMatrix, normalMatrix, consumer, -0.5F, -0.5F, 0.0F, minU, 1.0F, 255, 140, 30);
        vertex(poseMatrix, normalMatrix, consumer,  0.5F, -0.5F, 0.0F, maxU, 1.0F, 255, 140, 30);
        vertex(poseMatrix, normalMatrix, consumer,  0.5F,  0.5F, 0.0F, maxU, 0.0F, 255, 140, 30);
        vertex(poseMatrix, normalMatrix, consumer, -0.5F,  0.5F, 0.0F, minU, 0.0F, 255, 140, 30);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v,
                               int r, int g, int b) {
        builder.vertex(matrix, x, y, z)
                .color(r, g, b, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(RaiseDanmakuProjectile entity) {
        return TEXTURE;
    }
}
