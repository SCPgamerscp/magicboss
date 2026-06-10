package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SureHitEffect extends MagicMobEffect {

    public static final float GUIDED_MULTIPLIER = 10.0f;

    public SureHitEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
