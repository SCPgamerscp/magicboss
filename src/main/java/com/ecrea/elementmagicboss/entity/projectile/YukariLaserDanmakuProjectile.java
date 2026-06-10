package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.DustParticleOptions;
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

import javax.annotation.Nullable;

public class YukariLaserDanmakuProjectile extends DanmakuShotProjectile {
    private static final EntityDataAccessor<Integer> DATA_BULLET_TYPE =
            SynchedEntityData.defineId(YukariLaserDanmakuProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BULLET_COLOR =
            SynchedEntityData.defineId(YukariLaserDanmakuProjectile.class, EntityDataSerializers.INT);
    private static final Vector3f RED = new Vector3f(0.92f, 0.16f, 0.24f);
    private static final Vector3f BLUE = new Vector3f(0.18f, 0.38f, 0.92f);

    private int maxLifetimeTicks = 120;

    public enum BulletType {
        BUBBLE,
        MENTOS;

        public static BulletType byId(int id) {
            BulletType[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    public enum BulletColor {
        RED,
        BLUE;

        public static BulletColor byId(int id) {
            BulletColor[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    @SuppressWarnings("unchecked")
    public YukariLaserDanmakuProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(true);
        setDamageSpellId(ModSpells.MESH_OF_LIGHT_AND_DARKNESS.getId().toString());
    }

    public YukariLaserDanmakuProjectile(Level level, @Nullable Entity owner) {
        this(ModEntities.YUKARI_LASER_DANMAKU_PROJECTILE.get(), level);
        if (owner != null) {
            setOwner(owner);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BULLET_TYPE, BulletType.BUBBLE.ordinal());
        this.entityData.define(DATA_BULLET_COLOR, BulletColor.RED.ordinal());
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
        this.maxLifetimeTicks = Mth.clamp(ticks, 20, 240);
    }

    public BulletType getBulletType() {
        return BulletType.byId(this.entityData.get(DATA_BULLET_TYPE));
    }

    public void setBulletType(BulletType bulletType) {
        this.entityData.set(DATA_BULLET_TYPE, bulletType.ordinal());
        refreshDimensions();
    }

    public BulletColor getBulletColor() {
        return BulletColor.byId(this.entityData.get(DATA_BULLET_COLOR));
    }

    public void setBulletColor(BulletColor bulletColor) {
        this.entityData.set(DATA_BULLET_COLOR, bulletColor.ordinal());
    }

    public float getVisualScale() {
        return getBulletType() == BulletType.BUBBLE ? 1.0f : 0.72f;
    }

    public float getHitboxScale() {
        return getBulletType() == BulletType.BUBBLE ? 0.82f : 0.56f;
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
        Vector3f color = getBulletColor() == BulletColor.RED ? RED : BLUE;
        level().addParticle(new DustParticleOptions(color, 1.0f), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            Vector3f color = getBulletColor() == BulletColor.RED ? RED : BLUE;
            serverLevel.sendParticles(new DustParticleOptions(color, 1.15f), x, y, z, 8, 0.08, 0.08, 0.08, 0.01);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL,
                0.45f, 1.1f + level().getRandom().nextFloat() * 0.2f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.MESH_OF_LIGHT_AND_DARKNESS.get() : spell;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BulletType", getBulletType().ordinal());
        tag.putInt("BulletColor", getBulletColor().ordinal());
        tag.putInt("MaxLifetimeTicks", maxLifetimeTicks);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBulletType(BulletType.byId(tag.getInt("BulletType")));
        setBulletColor(BulletColor.byId(tag.getInt("BulletColor")));
        if (tag.contains("MaxLifetimeTicks")) {
            maxLifetimeTicks = tag.getInt("MaxLifetimeTicks");
        }
        refreshDimensions();
    }
}
