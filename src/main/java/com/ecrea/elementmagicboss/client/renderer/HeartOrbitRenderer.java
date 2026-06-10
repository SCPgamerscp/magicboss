package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.HeartOrbitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

// Renders each heart as a 0.5x0.5 billboard using soul{n}.png
public class HeartOrbitRenderer extends EntityRenderer<HeartOrbitEntity> {

    public HeartOrbitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HeartOrbitEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Billboard: always face camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        Matrix4f pose   = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        float s = 0.5f; // 0.5x0.5 size
        vc.vertex(pose, -s, -s, 0f).color(255,255,255,255)
          .uv(0f,1f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose,  s, -s, 0f).color(255,255,255,255)
          .uv(1f,1f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose,  s,  s, 0f).color(255,255,255,255)
          .uv(1f,0f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose, -s,  s, 0f).color(255,255,255,255)
          .uv(0f,0f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal,0f,1f,0f).endVertex();

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HeartOrbitEntity entity) {
        int idx = entity.getTextureIndex();
        return new ResourceLocation(ElementMagicBossMod.MOD_ID,
                "textures/entity/soul" + idx + ".png");
    }
}