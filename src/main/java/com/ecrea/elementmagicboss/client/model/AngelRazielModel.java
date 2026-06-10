package com.ecrea.elementmagicboss.client.model;

import com.ecrea.elementmagicboss.entity.AngelRazielEntity;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMobModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Angel RazielのGeckoLibモデルクラス
 * Iron's Spells 'n Spellbooksの標準的な人型モデル(abstract_casting_mob)を使用します
 */
public class AngelRazielModel extends AbstractSpellCastingMobModel {
    public static final ResourceLocation TEXTURE = new ResourceLocation("elementmagicboss", "textures/entity/angel_raziel.png");

    @Override
    public ResourceLocation getTextureResource(AbstractSpellCastingMob object) {
        return TEXTURE;
    }

    // モデルのリソース場所を指定 (独自モデルを使用)
    @Override
    public ResourceLocation getModelResource(AbstractSpellCastingMob object) {
        return new ResourceLocation("elementmagicboss", "geo/angel_raziel.geo.json");
    }

    // アニメーションのリソース場所を指定
    @Override
    public ResourceLocation getAnimationResource(AbstractSpellCastingMob animatable) {
        return new ResourceLocation("elementmagicboss", "animations/angel_raziel.animation.json");
    }
}
