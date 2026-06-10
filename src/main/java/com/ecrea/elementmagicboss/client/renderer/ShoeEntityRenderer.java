package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ShoeEntity;
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

// Renders ShoeEntity as a 1x1 billboard (2D PNG, always faces camera)
public class ShoeEntityRenderer extends EntityRenderer<ShoeEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/shoe.png");

    public ShoeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ShoeEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Billboard: face the camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        Matrix4f pose   = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        // 1x1 quad centered at entity origin
        // UV の U を反転してカメラビルボード時の左右反転を補正
        // BL
        vc.vertex(pose, -0.5f, -0.5f, 0f).color(255,255,255,255)
          .uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal, 0f, 1f, 0f).endVertex();
        // BR
        vc.vertex(pose,  0.5f, -0.5f, 0f).color(255,255,255,255)
          .uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal, 0f, 1f, 0f).endVertex();
        // TR
        vc.vertex(pose,  0.5f,  0.5f, 0f).color(255,255,255,255)
          .uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal, 0f, 1f, 0f).endVertex();
        // TL
        vc.vertex(pose, -0.5f,  0.5f, 0f).color(255,255,255,255)
          .uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(packedLight).normal(normal, 0f, 1f, 0f).endVertex();

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShoeEntity entity) {
        return TEXTURE;
    }
}