package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 無敵 - マーカーバフエフェクト
 * 9秒(180tick)ごとに10秒(200tick)の各バフを付与する。
 */
public class InvincibilityEffect extends MagicMobEffect {

    public InvincibilityEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 180tickごと(9秒ごと)に発火。duration=0は期限切れなので除外
        return duration > 0 && duration % 180 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        // Abyssal Shroud Lv1 (amplifier=0)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ABYSSAL_SHROUD.get(),   200,  0, false, true, true));
        // Echoing Strikes Lv30 (amplifier=29)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ECHOING_STRIKES.get(),  200, 29, false, true, true));
        // Haste Lv5 (amplifier=4)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.HASTENED.get(),         200,  4, false, true, true));
        // Charge Lv5 (amplifier=4)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.CHARGED.get(),          200,  4, false, true, true));
        // Thunderstorm Lv30 (amplifier=29)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.THUNDERSTORM.get(),     200, 29, false, true, true));
        // Oakskin Lv20 (amplifier=19)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.OAKSKIN.get(),          200, 19, false, true, true));
        // Spider Aspect Lv20 (amplifier=19)
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SPIDER_ASPECT.get(),    200, 19, false, true, true));
        // 再生 Lv5 (amplifier=4)
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,                  200,  4, false, true, true));
        // 耐性 Lv5 (amplifier=4)
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,             200,  4, false, true, true));
        // 攻撃力上昇 Lv5 (amplifier=4)
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,                  200,  4, false, true, true));
    }
}