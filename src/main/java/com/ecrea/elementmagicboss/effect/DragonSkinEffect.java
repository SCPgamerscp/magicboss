package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * ドラゴンスキン - バフエフェクト
 * 全ダメージ75%軽減 → ModEvents.onLivingHurt で event.setAmount(×0.25f)
 * 呪文耐性+100%     → ModMobEffects 登録時に addAttributeModifier(SPELL_RESIST, MULTIPLY_BASE, 1.0)
 */
public class DragonSkinEffect extends MagicMobEffect {

    public DragonSkinEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}