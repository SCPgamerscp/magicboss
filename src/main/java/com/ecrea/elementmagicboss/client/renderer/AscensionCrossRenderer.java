package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.AscensionCrossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AscensionCrossRenderer extends EntityRenderer<AscensionCrossEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/beacon_beam.png");

    public AscensionCrossRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(AscensionCrossEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.12f, 0.12f, 0.12f);
        MagicImpactCrossRenderer.renderCrossBars(
                poseStack, bufferSource,
                6.0f, 24.0f,
                16.0f, 4.0f,
                255, 248, 225, (int) (255.0f * entity.getAlpha(partialTick))
        );
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(AscensionCrossEntity entity) {
        return TEXTURE;
    }
}
