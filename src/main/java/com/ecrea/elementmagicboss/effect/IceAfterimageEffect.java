package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 氷残像バフ
 * このエフェクトを持つエンティティが移動すると、
 * 5tickごとに FrozenHumanoid（Frost Stepの氷の像）を召喚する。
 * amplifier 0 → shatterDamage = SpellPower (レベルで変わる)
 */
public class IceAfterimageEffect extends MagicMobEffect {

    public IceAfterimageEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
