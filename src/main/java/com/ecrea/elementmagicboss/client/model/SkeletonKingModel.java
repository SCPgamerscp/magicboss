package com.ecrea.elementmagicboss.client.model;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

public class SkeletonKingModel extends AbstractSpellCastingMobModel {

    public static final ResourceLocation MODEL = new ResourceLocation("elementmagicboss", "geo/sans.geo.json");
    public static final ResourceLocation TEXTURE = new ResourceLocation("elementmagicboss", "textures/entity/sans.png");

    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob animatable) {
        return TEXTURE;
    }
}
