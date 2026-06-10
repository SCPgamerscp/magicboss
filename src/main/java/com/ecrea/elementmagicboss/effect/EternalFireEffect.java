package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 永遠の火デバフ
 * - 炎上時に 2147483647 tick 燃え続ける
 * - 消火不可
 * - 炎上ダメージを multiplier 倍に増幅（LivingHurtEvent で処理）
 * amplifier 0 -> x2.0, amplifier N -> x(2.0 + N*0.5)
 */
public class EternalFireEffect extends MagicMobEffect {

    public EternalFireEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }
}