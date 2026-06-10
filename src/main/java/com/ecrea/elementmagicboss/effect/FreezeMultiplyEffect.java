package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 凍結倍化エフェクト
 * - 凍結進行速度を multiplier 倍に加速（LivingTickEvent で処理）
 * - 凍結ダメージを multiplier 倍に増幅（LivingHurtEvent で処理）
 * - 移動速度を -80% に低下（AttributeModifier で自動管理）
 * amplifier 0 -> x2.0, amplifier 1 -> x2.5, amplifier N -> x(2.0 + N*0.5)
 */
public class FreezeMultiplyEffect extends MagicMobEffect {

    public FreezeMultiplyEffect(MobEffectCategory category, int color) {
        super(category, color);
        // 移動速度 -80%（MULTIPLY_TOTAL: final = base * (1 + factor)）
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                "8e4a3e2b-7c1d-4f5a-9b8e-3c2d1a0f6e4b",
                -0.8,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    public static float getMultiplier(int amplifier) {
        return 2.0f + amplifier * 0.5f;
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        // 全レベルで一律 -80% にするため、amplifier で倍加させずに定数を返す
        return modifier.getAmount();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false; // tick 処理は LivingTickEvent で行う
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 使用しない
    }
}