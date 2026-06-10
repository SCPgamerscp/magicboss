package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * ダメージ倍化エフェクト
 * このエフェクトを持つエンティティが受けるダメージを増幅する。
 * amplifier 0 -> x2.0, amplifier 1 -> x2.5, amplifier N -> x(2.0 + N*0.5)
 * ModEvents.onLivingHurt で実際のダメージ計算を行う。
 */
public class DamageMultiplyEffect extends MagicMobEffect {

    public DamageMultiplyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** amplifier から倍率を計算するユーティリティ */
    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }
}