package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 濡れデバフ
 * 呪文耐性・氷呪文耐性・雷呪文耐性を低下させる。
 * ChargeEffectのパターンに準拠。
 * attribute modifierはModMobEffectsの登録時にMULTIPLY_BASEで設定。
 * Lv1(amp=0): -10%, Lv2(amp=1): -20%, ..., Lv10(amp=9): -100%
 */
public class WetEffect extends MagicMobEffect {

    /** 1レベルあたりの耐性低下率 (MULTIPLY_BASE用、-0.1 = -10%) */
    public static final float RESIST_REDUCTION_PER_LEVEL = -0.1f;

    public WetEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * amplifier に応じた耐性低下量を返す。
     * Lv1(amp=0)=-10%, Lv10(amp=9)=-100%
     */
    @Override
    public double getAttributeModifierValue(int amplifier, net.minecraft.world.entity.ai.attributes.AttributeModifier modifier) {
        return (amplifier + 1) * RESIST_REDUCTION_PER_LEVEL;
    }
}
