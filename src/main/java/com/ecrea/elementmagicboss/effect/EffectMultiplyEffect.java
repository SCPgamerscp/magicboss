package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * エフェクト倍化デバフ
 * このエフェクトを持つエンティティが新たにデバフを受けたとき、
 * そのデバフの持続時間を multiplier 倍に延長する。
 * MobEffectEvent.Added で処理。
 * amplifier 0 -> x2.0, amplifier 1 -> x2.5, amplifier N -> x(2.0 + N*0.5)
 */
public class EffectMultiplyEffect extends MagicMobEffect {

    public EffectMultiplyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }
}