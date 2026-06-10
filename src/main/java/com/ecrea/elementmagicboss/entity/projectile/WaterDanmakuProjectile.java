package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

/**
 * 弾幕雨用のプロジェクタイル。
 * DanmakuShotProjectile のサブクラス。
 * - 重力あり
 * - 水色・青系のパーティクル
 * - バウンスなし（雨らしく）
 */
public class WaterDanmakuProjectile extends DanmakuShotProjectile {

    public WaterDanmakuProjectile(EntityType<? extends DanmakuShotProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);   // 重力有効
    }

    public WaterDanmakuProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.WATER_DANMAKU_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    @Override
    public int getMaxBounces() {
        return 0;   // バウンスなし
    }

    @Override
    public int getMaxLifetimeTicks() {
        return 200;  // 10秒
    }

    @Override
    protected void trailParticles() {
        // 水色系ランダムパーティクル
        var pos = this.getBoundingBox().getCenter();
        double rand = level().random.nextDouble();
        if (rand < 0.33) {
            level().addParticle(ParticleTypes.DRIPPING_WATER,  pos.x, pos.y, pos.z, 0, 0, 0);
        } else if (rand < 0.66) {
            level().addParticle(ParticleTypes.FALLING_WATER,   pos.x, pos.y, pos.z, 0, 0, 0);
        } else {
            level().addParticle(ParticleTypes.SPLASH,          pos.x, pos.y, pos.z, 0, 0, 0);
        }
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH,      x, y, z, 8,  0.1, 0.1, 0.1, 0.1);
            serverLevel.sendParticles(ParticleTypes.FALLING_WATER, x, y, z, 4, 0.05, 0.05, 0.05, 0.02);
        }
    }
}
