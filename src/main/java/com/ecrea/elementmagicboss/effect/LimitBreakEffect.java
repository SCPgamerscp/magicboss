package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * リミットブレイクバフ
 * - 詠唱時間短縮 +100% (上限)
 * - クールダウン短縮 +100% (上限)
 * - マナ再生 +100% (上限)
 * AttributeModifier で自動管理 (エフェクト消滅と同時に解除)
 */
public class LimitBreakEffect extends MagicMobEffect {

    public LimitBreakEffect(MobEffectCategory category, int color) {
        super(category, color);
        // 詠唱時間短縮: +999 (内部キャップ100で処理)
        this.addAttributeModifier(
                AttributeRegistry.CAST_TIME_REDUCTION.get(),
                "a1b2c3d4-1001-1001-1001-000000000001",
                999.0,
                AttributeModifier.Operation.ADDITION
        );
        // クールダウン短縮: +999 (内部キャップ100で処理)
        this.addAttributeModifier(
                AttributeRegistry.COOLDOWN_REDUCTION.get(),
                "a1b2c3d4-2002-2002-2002-000000000002",
                999.0,
                AttributeModifier.Operation.ADDITION
        );
        // マナ再生: +999 (内部キャップ100で処理)
        this.addAttributeModifier(
                AttributeRegistry.MANA_REGEN.get(),
                "a1b2c3d4-3003-3003-3003-000000000003",
                999.0,
                AttributeModifier.Operation.ADDITION
        );
    }
}
