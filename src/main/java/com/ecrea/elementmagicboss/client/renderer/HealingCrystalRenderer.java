package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.HealingCrystalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

import java.util.Optional;

// Delegate to vanilla EndCrystalRenderer by creating a proxy EndCrystal on the fly
public class HealingCrystalRenderer extends EntityRenderer<HealingCrystalEntity> {

    private final EndCrystalRenderer delegate;

    public HealingCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.delegate = new EndCrystalRenderer(context);
    }

    @Override
    public void render(HealingCrystalEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Create a transient EndCrystal to pass to vanilla renderer
        EndCrystal proxy = new EndCrystal(EntityType.END_CRYSTAL, entity.level());
        proxy.setPos(entity.getX(), entity.getY(), entity.getZ());
        proxy.tickCount = entity.tickCount;
        proxy.time = entity.tickCount;

        // Set beam target from synced data
        Optional<BlockPos> beam = entity.getBeamTarget();
        beam.ifPresent(proxy::setBeamTarget);

        delegate.render(proxy, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HealingCrystalEntity entity) {
        return new ResourceLocation("minecraft", "textures/entity/end_crystal/end_crystal.png");
    }
}
