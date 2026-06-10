package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 神の祝福バフ
 * - 受ける回復量を multiplier 倍に増幅（LivingHealEvent で処理）
 * - 与えるダメージを multiplier 倍に増幅（LivingHurtEvent で処理）
 * amplifier 0 -> x2.0, amplifier N -> x(2.0 + N*0.5)
 */
public class DivineBlessingEffect extends MagicMobEffect {

    public DivineBlessingEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }
}
