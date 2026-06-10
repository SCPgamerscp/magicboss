package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.YinYangBallEntity;
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

public class YinYangBallRenderer extends EntityRenderer<YinYangBallEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/yin_yang_ball.png");

    public YinYangBallRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(YinYangBallEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Spin animation
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(entity.tickCount * 10f));

        Matrix4f pose   = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        float s = 0.5f;
        vc.vertex(pose, -s, -s, 0f).color(255,255,255,255).uv(0f,1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose,  s, -s, 0f).color(255,255,255,255).uv(1f,1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose,  s,  s, 0f).color(255,255,255,255).uv(1f,0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0f,1f,0f).endVertex();
        vc.vertex(pose, -s,  s, 0f).color(255,255,255,255).uv(0f,0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0f,1f,0f).endVertex();

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(YinYangBallEntity entity) {
        return TEXTURE;
    }
}