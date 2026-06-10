package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ElementEntity;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import io.redspace.ironsspellbooks.entity.mobs.wizards.pyromancer.PyromancerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ElementEntityRenderer extends AbstractSpellCastingMobRenderer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/element_boss.png");

    public ElementEntityRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PyromancerModel());
    }

    @Override
    public ResourceLocation getTextureLocation(io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob pEntity) {
        return TEXTURE;
    }
}
