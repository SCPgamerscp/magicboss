package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.TrueArrowVolleyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class TrueArrowVolleyRenderer extends EntityRenderer<TrueArrowVolleyEntity> {

    public TrueArrowVolleyRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TrueArrowVolleyEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(TrueArrowVolleyEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
