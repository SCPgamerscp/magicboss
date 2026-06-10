package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

// Musou: cast time reduction +50%, cooldown reduction +50%, attack +50%,
// mana regen +50%, spell resist +50% via attributes.
// Heal boost and damage boost handled by SealHandler.
public class MusouEffect extends MagicMobEffect {
    public MusouEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.addAttributeModifier(AttributeRegistry.CAST_TIME_REDUCTION.get(),
                "a1b2c3d4-e5f6-7890-abcd-ef1234567880",
                0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                "a1b2c3d4-e5f6-7890-abcd-ef1234567881",
                0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "a1b2c3d4-e5f6-7890-abcd-ef1234567882",
                0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(AttributeRegistry.MANA_REGEN.get(),
                "a1b2c3d4-e5f6-7890-abcd-ef1234567883",
                0.5, AttributeModifier.Operation.MULTIPLY_BASE);
        this.addAttributeModifier(AttributeRegistry.SPELL_RESIST.get(),
                "a1b2c3d4-e5f6-7890-abcd-ef1234567884",
                0.5, AttributeModifier.Operation.MULTIPLY_BASE);
    }
}