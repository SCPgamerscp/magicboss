package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

public class ScarletMeisterProjectile extends DanmakuShotProjectile {
    private static final EntityDataAccessor<Integer> DATA_BULLET_TYPE =
            SynchedEntityData.defineId(ScarletMeisterProjectile.class, EntityDataSerializers.INT);
    private static final Vector3f SCARLET = new Vector3f(0.95f, 0.12f, 0.22f);

    private int maxLifetimeTicks = 200;

    public enum BulletType {
        BUBBLE,
        MENTOS,
        BALL;

        public static BulletType byId(int id) {
            BulletType[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    @SuppressWarnings("unchecked")
    public ScarletMeisterProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(true);
        setDamageSpellId(ModSpells.SCARLET_MEISTER.getId().toString());
    }

    public ScarletMeisterProjectile(Level level, Entity owner) {
        this(ModEntities.SCARLET_MEISTER_PROJECTILE.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BULLET_TYPE, BulletType.BUBBLE.ordinal());
    }

    @Override
    public int getMaxBounces() {
        return 0;
    }

    @Override
    public int getMaxLifetimeTicks() {
        return maxLifetimeTicks;
    }

    public void setMaxLifetimeTicksCustom(int ticks) {
        this.maxLifetimeTicks = Mth.clamp(ticks, 10, 240);
    }

    public BulletType getBulletType() {
        return BulletType.byId(this.entityData.get(DATA_BULLET_TYPE));
    }

    public void setBulletType(BulletType bulletType) {
        this.entityData.set(DATA_BULLET_TYPE, bulletType.ordinal());
        refreshDimensions();
    }

    public float getVisualScale() {
        return switch (getBulletType()) {
            case BUBBLE -> 1.0f;
            case MENTOS -> 0.72f;
            case BALL -> 0.48f;
        };
    }

    public float getHitboxScale() {
        return switch (getBulletType()) {
            case BUBBLE -> 0.80f;
            case MENTOS -> 0.56f;
            case BALL -> 0.34f;
        };
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float size = getHitboxScale();
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_BULLET_TYPE.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();
        discard();
    }

    @Override
    protected void trailParticles() {
        var pos = getBoundingBox().getCenter();
        level().addParticle(new DustParticleOptions(SCARLET, 1.0f), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
        if (level().random.nextBoolean()) {
            level().addParticle(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(SCARLET, 1.1f), x, y, z, 8, 0.08, 0.08, 0.08, 0.01);
            serverLevel.sendParticles(ParticleTypes.CRIT, x, y, z, 6, 0.06, 0.06, 0.06, 0.01);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL,
                0.45f, 1.2f + level().getRandom().nextFloat() * 0.2f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.SCARLET_MEISTER.get() : spell;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BulletType", getBulletType().ordinal());
        tag.putInt("MaxLifetimeTicks", maxLifetimeTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBulletType(BulletType.byId(tag.getInt("BulletType")));
        if (tag.contains("MaxLifetimeTicks")) {
            maxLifetimeTicks = tag.getInt("MaxLifetimeTicks");
        }
        refreshDimensions();
    }
}
