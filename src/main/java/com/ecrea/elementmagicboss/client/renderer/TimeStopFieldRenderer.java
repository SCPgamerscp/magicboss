package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.TimeStopFieldEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TimeStopFieldRenderer extends EntityRenderer<TimeStopFieldEntity> {
    public TimeStopFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(TimeStopFieldEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(TimeStopFieldEntity entity) {
        return new ResourceLocation("minecraft", "textures/misc/white.png");
    }
}
