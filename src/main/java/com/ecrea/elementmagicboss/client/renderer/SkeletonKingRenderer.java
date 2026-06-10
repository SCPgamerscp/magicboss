package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.client.model.SkeletonKingModel;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class SkeletonKingRenderer extends AbstractSpellCastingMobRenderer {
    public SkeletonKingRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SkeletonKingModel());
    }
}
