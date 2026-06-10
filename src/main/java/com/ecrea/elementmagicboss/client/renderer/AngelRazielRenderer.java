package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.client.model.AngelRazielModel;
import com.ecrea.elementmagicboss.entity.AngelRazielEntity;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Angel Razielのレンダラークラス
 * AbstractSpellCastingMobRendererを継承することで、魔法詠唱アニメーションや
 * 装備の描画をIron's Spells 'n Spellbooksの標準に合わせます。
 */
public class AngelRazielRenderer extends AbstractSpellCastingMobRenderer {
    
    public AngelRazielRenderer(EntityRendererProvider.Context context) {
        super(context, new AngelRazielModel());
    }

    @Override
    public ResourceLocation getTextureLocation(io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob entity) {
        return AngelRazielModel.TEXTURE;
    }
}
