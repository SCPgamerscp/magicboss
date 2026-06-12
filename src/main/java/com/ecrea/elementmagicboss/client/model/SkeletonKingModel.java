package com.ecrea.elementmagicboss.client.model;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class SkeletonKingModel extends AbstractSpellCastingMobModel {

    public static final ResourceLocation MODEL = new ResourceLocation("elementmagicboss", "geo/sans.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation("elementmagicboss", "textures/entity/sans.png");
    public static final ResourceLocation ANIMATION = new ResourceLocation("elementmagicboss", "animations/sans.animation.json");

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return ANIMATION;
    }
}
