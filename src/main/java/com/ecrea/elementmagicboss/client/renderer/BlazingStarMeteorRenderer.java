package com.ecrea.elementmagicboss.client.renderer;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.fireball.FireballRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;

public class BlazingStarMeteorRenderer extends FireballRenderer {
    private static final ResourceLocation BASE_TEXTURE = IronsSpellbooks.id("textures/entity/comet/comet.png");
    private static final ResourceLocation[] FIRE_TEXTURES = {
            IronsSpellbooks.id("textures/entity/comet/fire_1.png"),
            IronsSpellbooks.id("textures/entity/comet/fire_2.png"),
            IronsSpellbooks.id("textures/entity/comet/fire_3.png"),
            IronsSpellbooks.id("textures/entity/comet/fire_4.png")
    };

    public BlazingStarMeteorRenderer(EntityRendererProvider.Context context) {
        super(context, 0.75F);
    }

    @Override
    public ResourceLocation getTextureLocation(Projectile entity) {
        return BASE_TEXTURE;
    }

    @Override
    public ResourceLocation getFireTextureLocation(Projectile entity) {
        return FIRE_TEXTURES[(entity.tickCount / 2) % FIRE_TEXTURES.length];
    }
}