package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.ArrowStormArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.entity.spells.magic_arrow.MagicArrowRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * MagicArrowRendererと完全に同じ向き計算を使用する。
 */
public class ArrowStormArrowRenderer extends EntityRenderer<ArrowStormArrow> {

    public ArrowStormArrowRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ArrowStormArrow entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // MagicArrowRenderer#render と完全に同じ向き計算
        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float)(Mth.atan2(motion.horizontalDistance(), motion.y)
                * (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float)(Mth.atan2(motion.z, motion.x)
                * (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        MagicArrowRenderer.renderModel(poseStack, buffer);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ArrowStormArrow entity) {
        return MagicArrowRenderer.getTextureLocation();
    }
}
