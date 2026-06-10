package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ResourceGlowMarkerEntity;
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

/**
 * Renders a small 1x1 translucent billboard so the vanilla glow outline system
 * has geometry to trace. The billboard is nearly invisible (alpha=4) but sufficient
 * for the outline pass.
 */
public class ResourceGlowMarkerRenderer extends EntityRenderer<ResourceGlowMarkerEntity> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/block/white_concrete.png");

    public ResourceGlowMarkerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ResourceGlowMarkerEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Render a 1x1x1 cube face (top face) centered at block center
        // Just need enough geometry for the outline pass
        Matrix4f pose   = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        // Top face of 1x1x1 cube (y=1, x/z from -0.5 to 0.5)
        float a = 4; // nearly transparent (4/255)
        vc.vertex(pose, -0.5f, 1.0f, -0.5f).color(255,255,255,a).uv(0,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0,1,0).endVertex();
        vc.vertex(pose,  0.5f, 1.0f, -0.5f).color(255,255,255,a).uv(1,0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0,1,0).endVertex();
        vc.vertex(pose,  0.5f, 1.0f,  0.5f).color(255,255,255,a).uv(1,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0,1,0).endVertex();
        vc.vertex(pose, -0.5f, 1.0f,  0.5f).color(255,255,255,a).uv(0,1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal,0,1,0).endVertex();

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ResourceGlowMarkerEntity entity) {
        return TEXTURE;
    }
}