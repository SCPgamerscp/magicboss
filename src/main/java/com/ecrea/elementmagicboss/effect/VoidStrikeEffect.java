package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * ボイドストライク - マーカーバフエフェクト
 * 付与中、詠唱者の全攻撃が奈落ダメージ(outOfWorld)に変換される。
 * ダメージ変換ロジックは VoidStrikeHandler で処理。
 */
public class VoidStrikeEffect extends MagicMobEffect {

    public VoidStrikeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}