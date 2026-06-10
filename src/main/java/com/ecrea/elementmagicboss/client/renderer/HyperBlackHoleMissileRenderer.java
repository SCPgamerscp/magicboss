package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.HyperBlackHoleMissile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * MagicMissileRendererと同じ描画ロジックを使用する。
 */
public class HyperBlackHoleMissileRenderer extends EntityRenderer<HyperBlackHoleMissile> {

    private static final ResourceLocation TEXTURE =
            IronsSpellbooks.id("textures/entity/magic_missile/magic_missile.png");
    private static final ResourceLocation FLARE =
            IronsSpellbooks.id("textures/entity/lens_flare.png");

    private final ModelPart body;

    public HyperBlackHoleMissileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        ModelPart modelpart = ctx.bakeLayer(FireballRenderer.MODEL_LAYER_LOCATION);
        this.body = modelpart.getChild("body");
    }


    @Override
    public void render(HyperBlackHoleMissile entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // MagicMissileRendererと同じ描画（向き + モデル + フレア）
        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float)(Mth.atan2(motion.horizontalDistance(), motion.y)
                * (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float)(Mth.atan2(motion.z, motion.x)
                * (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.scale(0.5f, 0.5f, 0.5f); // 少し大きめ

        VertexConsumer consumer = buffer.getBuffer(RenderType.energySwirl(TEXTURE, 0, 0));
        body.render(poseStack, consumer, LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, 0.8f, 0.8f, 0.8f, 1f);
        poseStack.popPose();

        // フレアエフェクト（MagicMissile準拠）
        poseStack.pushPose();
        float f = entity.tickCount + partialTick;
        float scale = 0.5f + Mth.sin(f) * 0.125f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(90f));
        poseStack.mulPose(Axis.XP.rotationDegrees(f * 15));
        Matrix4f mat = poseStack.last().pose();
        VertexConsumer flare = buffer.getBuffer(RenderType.entityTranslucent(FLARE));
        flare.vertex(mat, 0, -1, -1).color(150, 0, 255, 255).uv(0f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        flare.vertex(mat, 0,  1, -1).color(150, 0, 255, 255).uv(0f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        flare.vertex(mat, 0,  1,  1).color(150, 0, 255, 255).uv(1f, 0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        flare.vertex(mat, 0, -1,  1).color(150, 0, 255, 255).uv(1f, 1f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(0f, 1f, 0f).endVertex();
        poseStack.popPose();

        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HyperBlackHoleMissile entity) {
        return TEXTURE;
    }
}
