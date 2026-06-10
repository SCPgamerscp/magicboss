package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 剣の舞マーカーエフェクト
 * エンティティスポーンは KenNoMaiSpell.onCast() で行う。
 * このエフェクトは SpinningSwordEntity が「まだ有効か」を判定するためだけに使用する。
 */
public class KenNoMaiEffect extends MagicMobEffect {

    public KenNoMaiEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
