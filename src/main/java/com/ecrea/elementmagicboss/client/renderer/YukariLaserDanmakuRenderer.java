package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.YukariLaserDanmakuProjectile;
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

public class YukariLaserDanmakuRenderer extends EntityRenderer<YukariLaserDanmakuProjectile> {
    private static final ResourceLocation RED_BUBBLE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/yukari_bubble_red.png");
    private static final ResourceLocation BLUE_BUBBLE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/yukari_bubble_blue.png");
    private static final ResourceLocation RED_MENTOS =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/yukari_mentos_red.png");
    private static final ResourceLocation BLUE_MENTOS =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/yukari_mentos_blue.png");

    public YukariLaserDanmakuRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(YukariLaserDanmakuProjectile entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        float scale = entity.getVisualScale();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        renderSprite(poseStack, bufferSource, getTextureLocation(entity));
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private static void renderSprite(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture) {
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(texture));

        vertex(poseMatrix, normalMatrix, consumer, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f);
        vertex(poseMatrix, normalMatrix, consumer, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f);
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
    public ResourceLocation getTextureLocation(YukariLaserDanmakuProjectile entity) {
        return switch (entity.getBulletType()) {
            case BUBBLE -> entity.getBulletColor() == YukariLaserDanmakuProjectile.BulletColor.RED ? RED_BUBBLE : BLUE_BUBBLE;
            case MENTOS -> entity.getBulletColor() == YukariLaserDanmakuProjectile.BulletColor.RED ? RED_MENTOS : BLUE_MENTOS;
        };
    }
}
