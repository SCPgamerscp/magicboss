package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.FireFryingPanEntity;
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

public class FireFryingPanRenderer extends EntityRenderer<FireFryingPanEntity> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/fire_frying_pan_0.png"),
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/fire_frying_pan_1.png")
    };
    private static final float[] FRAME_WIDTHS = new float[]{95f, 62f};
    private static final float[] FRAME_HEIGHTS = new float[]{17f, 58f};
    private static final float MAX_FRAME_WIDTH = 95f;
    private static final float MAX_FRAME_HEIGHT = 58f;

    public FireFryingPanRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(FireFryingPanEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.scale(2.8f, 2.8f, 2.8f);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        int frame = entity.getAnimationFrame();
        ResourceLocation texture = TEXTURES[frame];
        float halfWidth = 0.5f * (FRAME_WIDTHS[frame] / MAX_FRAME_WIDTH);
        float halfHeight = 0.5f * (FRAME_HEIGHTS[frame] / MAX_FRAME_HEIGHT);
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        vertex(poseMatrix, normalMatrix, consumer, -halfWidth, -halfHeight, 0.0f, 1.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, halfWidth, -halfHeight, 0.0f, 0.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, halfWidth, halfHeight, 0.0f, 0.0f, 0.0f);
        vertex(poseMatrix, normalMatrix, consumer, -halfWidth, halfHeight, 0.0f, 1.0f, 0.0f);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, light);
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
    public ResourceLocation getTextureLocation(FireFryingPanEntity entity) {
        return TEXTURES[entity.getAnimationFrame()];
    }
}
