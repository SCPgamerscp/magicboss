package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * 使徒光線で出現する光の十字架エンティティ。
 * 爆発地点に出現し、10秒間周囲の敵にダメージを与え続ける。
 */
public class ApostleBeamCrossEntity extends Entity {
    /** ダメージ半径 */
    public static final float DAMAGE_RADIUS = 20.0f;
    /** 十字架の高さ (描画用) */
    public static final float CROSS_HEIGHT = 30.0f;
    /** 十字架の幅 (描画用) */
    public static final float CROSS_WIDTH = 15.0f;
    /** 持続時間 (10秒) */
    public static final int DURATION_TICKS = 20 * 10;
    /** ダメージ間隔 */
    public static final int DAMAGE_INTERVAL_TICKS = 5;

    private UUID ownerUUID;
    private float spellDamage;

    public ApostleBeamCrossEntity(EntityType<? extends ApostleBeamCrossEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnAmbientClientParticles();
            return;
        }

        if (tickCount >= DURATION_TICKS) {
            discard();
            return;
        }

        // 継続ダメージ
        if (tickCount % DAMAGE_INTERVAL_TICKS == 0) {
            applyAreaDamage();
        }

        // サーバーサイドパーティクル (光の柱)
        if (tickCount % 5 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY() + CROSS_HEIGHT * 0.5, getZ(),
                    30, 2.0, CROSS_HEIGHT * 0.35, 2.0, 0.03);
            serverLevel.sendParticles(ParticleTypes.GLOW,
                    getX(), getY() + 2.0, getZ(),
                    20, 1.5, 1.5, 1.5, 0.02);
            // 横棒のパーティクル
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY() + CROSS_HEIGHT * 0.6, getZ(),
                    20, CROSS_WIDTH * 0.4, 0.5, 0.5, 0.01);
        }
    }

    private void applyAreaDamage() {
        LivingEntity owner = getOwnerEntity();
        AABB area = AABB.ofSize(position(), DAMAGE_RADIUS * 2.0, DAMAGE_RADIUS * 2.0, DAMAGE_RADIUS * 2.0);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, living ->
                living.isAlive()
                        && living != owner
                        && living.distanceToSqr(position()) <= DAMAGE_RADIUS * DAMAGE_RADIUS
                        && !isFriendly(owner, living));

        Entity attacker = owner != null ? owner : this;
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            DamageSources.applyDamage(
                    target,
                    spellDamage,
                    ModSpells.APOSTLE_BEAM.get().getDamageSource(this, attacker)
            );
            target.invulnerableTime = 0;
        }
    }

    private boolean isFriendly(@Nullable LivingEntity owner, LivingEntity target) {
        return owner != null && DamageSources.isFriendlyFireBetween(owner, target);
    }

    private void spawnAmbientClientParticles() {
        RandomSource random = level().getRandom();
        // 縦のパーティクル
        for (int i = 0; i < 8; i++) {
            double y = getY() + random.nextDouble() * CROSS_HEIGHT;
            double x = getX() + (random.nextDouble() - 0.5) * 3.0;
            double z = getZ() + (random.nextDouble() - 0.5) * 3.0;
            level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.05, 0.0);
        }
        // 横のパーティクル
        for (int i = 0; i < 4; i++) {
            double x = getX() + (random.nextDouble() - 0.5) * CROSS_WIDTH;
            double y = getY() + CROSS_HEIGHT * 0.6 + (random.nextDouble() - 0.5) * 2.0;
            double z = getZ() + (random.nextDouble() - 0.5) * 3.0;
            level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    public void setSpellDamage(float spellDamage) {
        this.spellDamage = spellDamage;
    }

    public float getAgeScale(float partialTick) {
        float age = tickCount + partialTick;
        if (age < 15.0f) {
            return age / 15.0f;
        }
        // 消滅前のフェードアウト (残り40tick)
        float remaining = DURATION_TICKS - age;
        if (remaining < 40.0f && remaining > 0.0f) {
            return remaining / 40.0f;
        }
        return 1.0f;
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUUID = owner.getUUID();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        spellDamage = tag.getFloat("SpellDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("SpellDamage", spellDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 512.0D * 512.0D;
    }
}
