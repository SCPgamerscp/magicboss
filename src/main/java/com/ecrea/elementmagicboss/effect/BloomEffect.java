package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BloomEffect extends MagicMobEffect {

    public BloomEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        DamageSource src = new BloomDamageSource(entity);
        entity.hurt(src, 6.0f);
    }

    // Inner damage source class for custom death message
    public static class BloomDamageSource extends DamageSource {
        public BloomDamageSource(LivingEntity victim) {
            super(victim.level().damageSources().magic().typeHolder());
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity killed) {
            return Component.translatable("death.attack.bloom", killed.getDisplayName());
        }
    }
}