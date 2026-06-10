package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class CurseOfBleedingEffect extends MobEffect {

    public CurseOfBleedingEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A0303); // Blood red color
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Tick every tick to check movement
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            // 水平方向の移動速度をチェック (落下や垂直ジャンプのみは対象外とする)
            double horizontalSpeedSqr = entity.getDeltaMovement().x * entity.getDeltaMovement().x 
                                      + entity.getDeltaMovement().z * entity.getDeltaMovement().z;
            
            // 移動している場合
            if (horizontalSpeedSqr > 0.0005) {
                // 10tick(0.5秒)に1回ダメージ
                if (entity.tickCount % 10 == 0) {
                    // ダメージをレベル(amplifier)に応じて増加させる
                    // Lv1(amp0)=2.0, Lv2(amp1)=4.0, ... Lv10(amp9)=20.0
                    float damage = 2.0f + (amplifier * 2.0f);
                    
                    // ダメージによる無敵時間をリセットして確実にダメージを与える
                    entity.invulnerableTime = 0;
                    entity.hurt(entity.damageSources().magic(), damage);
                }
            }
        }
    }
}
