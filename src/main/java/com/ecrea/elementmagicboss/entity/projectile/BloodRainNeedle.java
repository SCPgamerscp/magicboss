package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class BloodRainNeedle extends BloodNeedle {

    /** Forgeがスポーンパケットから復元する際に使うコンストラクタ */
    public BloodRainNeedle(EntityType<? extends BloodNeedle> entityType, Level level) {
        super(entityType, level);
    }

    /** スペルから直接生成する際に使うコンストラクタ */
    public BloodRainNeedle(Level level, LivingEntity shooter) {
        this(ModEntities.BLOOD_RAIN_NEEDLE.get(), level);
        setOwner(shooter);
    }

    /**
     * ダメージをBloodRainSpellのダメージソース（50%ライフスティール、iフレームなし）で適用する。
     * onHitEntityはクライアントでも呼ばれる場合があるため、必ずサーバー側のみ処理する。
     */
    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        // クライアント側では何もしない（level.getServer()がnullになりクラッシュする）
        if (level().isClientSide()) return;

        DamageSources.applyDamage(
                entityHitResult.getEntity(),
                getDamage(),
                ModSpells.BLOOD_RAIN.get().getDamageSource(this, getOwner())
        );
    }

    /**
     * 着弾パーティクル。AbstractMagicProjectileのonHit内（サーバー側）から呼ばれる。
     */
    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level(),
                ParticleHelper.BLOOD,
                x, y, z,
                15, .1, .1, .1, .18, true);
    }
}
