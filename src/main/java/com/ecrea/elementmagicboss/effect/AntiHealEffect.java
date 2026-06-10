package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

// AntiHeal: marker debuff. Damage logic is in AntiHealHandler.
public class AntiHealEffect extends MagicMobEffect {
    public AntiHealEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}