package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.HyperBlackHoleEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHoleRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleRenderer;

public class HyperBlackHoleRenderer extends BlackHoleRenderer {
    public HyperBlackHoleRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }
    @Override
    public void render(io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole entity,
                       float yaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int light) {
        super.render(entity, yaw, partialTicks, poseStack, buffer, light);
    }
    @Override
    public ResourceLocation getTextureLocation(io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole e) {
        return IcicleRenderer.TEXTURE;
    }
}
