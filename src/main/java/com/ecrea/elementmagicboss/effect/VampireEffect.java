package com.ecrea.elementmagicboss.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class VampireEffect extends MobEffect {

    public VampireEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xAA0000); // Vampire red
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // No tick logic needed, handled entirely in ModEvents via LivingDamageEvent
        return false;
    }
}
