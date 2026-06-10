package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// Seal: attack -50%, spell resist -50%, mana regen -50% via attributes.
// Heal reduction and damage reduction handled by SealHandler (LivingHurtEvent/LivingHealEvent).
public class SealEffect extends MagicMobEffect {
    public SealEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "b1a2c3d4-e5f6-7890-abcd-ef1234567890",
                -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(),
                "b1a2c3d4-e5f6-7890-abcd-ef1234567891",
                -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(AttributeRegistry.MANA_REGEN.get(),
                "b1a2c3d4-e5f6-7890-abcd-ef1234567892",
                -0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}