package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.ScarletMeisterSweepEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ScarletMeisterSweepRenderer extends EntityRenderer<ScarletMeisterSweepEntity> {
    private static final ResourceLocation DUMMY_TEXTURE = new ResourceLocation("minecraft", "textures/misc/white.png");

    public ScarletMeisterSweepRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(ScarletMeisterSweepEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(ScarletMeisterSweepEntity entity) {
        return DUMMY_TEXTURE;
    }
}