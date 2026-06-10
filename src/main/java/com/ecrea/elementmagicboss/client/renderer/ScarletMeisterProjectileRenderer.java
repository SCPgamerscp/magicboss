package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ScarletMeisterProjectile;
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

public class ScarletMeisterProjectileRenderer extends EntityRenderer<ScarletMeisterProjectile> {
    private static final ResourceLocation BUBBLE_TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/scarlet_meister_bubble.png");
    private static final ResourceLocation MENTOS_TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/scarlet_meister_mentos.png");
    private static final ResourceLocation BALL_TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/scarlet_meister_ball.png");

    public ScarletMeisterProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(ScarletMeisterProjectile entity, float yaw, float partialTicks,
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
    public ResourceLocation getTextureLocation(ScarletMeisterProjectile entity) {
        return switch (entity.getBulletType()) {
            case BUBBLE -> BUBBLE_TEXTURE;
            case MENTOS -> MENTOS_TEXTURE;
            case BALL -> BALL_TEXTURE;
        };
    }
}
