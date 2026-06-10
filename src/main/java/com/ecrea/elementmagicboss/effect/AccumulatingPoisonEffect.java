package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 蓄積する毒デバフ
 * このエフェクトを持つエンティティが毒を受けたとき、
 * 毒の持続時間を上書きせず加算する。
 * 毒ダメージも multiplier 倍に増幅する。
 * amplifier 0 -> x2.0, amplifier N -> x(2.0 + N*0.5)
 */
public class AccumulatingPoisonEffect extends MagicMobEffect {

    public AccumulatingPoisonEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }
}
