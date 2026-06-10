package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.DanmakuMeisterSpell;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DanmakuMeisterProjectile extends ScarletMeisterProjectile {
    private static final EntityDataAccessor<Integer> DATA_BULLET_COLOR =
            SynchedEntityData.defineId(DanmakuMeisterProjectile.class, EntityDataSerializers.INT);

    public enum BulletColor {
        BLUE(0x4E7BFF, TouhouLaserEntity.LaserColor.BLUE),
        GREEN(0x45FF79, TouhouLaserEntity.LaserColor.GREEN),
        RED(0xFF4A56, TouhouLaserEntity.LaserColor.RED),
        CYAN(0x4FFFFF, TouhouLaserEntity.LaserColor.CYAN),
        MAGENTA(0xFF4CFF, TouhouLaserEntity.LaserColor.MAGENTA),
        YELLOW(0xFFE760, TouhouLaserEntity.LaserColor.YELLOW);

        private final int rgb;
        private final TouhouLaserEntity.LaserColor laserColor;

        BulletColor(int rgb, TouhouLaserEntity.LaserColor laserColor) {
            this.rgb = rgb;
            this.laserColor = laserColor;
        }

        public int rgb() {
            return rgb;
        }

        public TouhouLaserEntity.LaserColor laserColor() {
            return laserColor;
        }

        public BulletColor next() {
            return byId(ordinal() + 1);
        }

        public static BulletColor byId(int id) {
            BulletColor[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    @SuppressWarnings("unchecked")
    public DanmakuMeisterProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends Projectile>) entityType, level);
        setDamageSpellId(ModSpells.DANMAKU_MEISTER.getId().toString());
    }

    public DanmakuMeisterProjectile(Level level, Entity owner) {
        this(ModEntities.DANMAKU_MEISTER_PROJECTILE.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BULLET_COLOR, BulletColor.RED.ordinal());
    }

    public BulletColor getBulletColor() {
        return BulletColor.byId(this.entityData.get(DATA_BULLET_COLOR));
    }

    public void setBulletColor(BulletColor color) {
        this.entityData.set(DATA_BULLET_COLOR, color.ordinal());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Vec3 hit = result.getLocation();
        super.onHitEntity(result);
        spawnLaserBurst(hit);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        Vec3 hit = result.getLocation();
        impactParticles(hit.x, hit.y, hit.z);
        playImpactSound();
        spawnLaserBurst(hit);
        discard();
    }

    private void spawnLaserBurst(Vec3 origin) {
        if (level().isClientSide) {
            return;
        }
        Entity owner = getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity living ? living : null;
        BulletColor color = getBulletColor();
        for (Vec3 dir : burstDirections()) {
            TouhouLaserEntity laser = new TouhouLaserEntity(level(), livingOwner, origin, dir, getDamage(), 100.0f, 120, 1,
                    color.laserColor(), ModSpells.DANMAKU_MEISTER.getId().toString());
            laser.addTag(DanmakuMeisterSpell.DANMAKU_MEISTER_LASER_TAG);
            laser.addTag(TouhouLaserEntity.IFRAME_TAG);
            laser.setRollSeed(level().getRandom().nextFloat());
            level().addFreshEntity(laser);
            color = color.next();
        }
    }

    private static List<Vec3> burstDirections() {
        ArrayList<Vec3> directions = new ArrayList<>(26);
        directions.add(new Vec3(0.0D, -1.0D, 0.0D));
        for (int pitch : new int[]{-45, 0, 45}) {
            double pitchRad = Math.toRadians(pitch);
            double y = Math.sin(pitchRad);
            double horizontal = Math.cos(pitchRad);
            for (int yaw = 0; yaw < 360; yaw += 45) {
                double yawRad = Math.toRadians(yaw);
                directions.add(new Vec3(Math.cos(yawRad) * horizontal, y, Math.sin(yawRad) * horizontal).normalize());
            }
        }
        directions.add(new Vec3(0.0D, 1.0D, 0.0D));
        return directions;
    }

    @Override
    protected void trailParticles() {
        int rgb = getBulletColor().rgb();
        Vector3f color = rgbToVector(rgb);
        Vec3 pos = getBoundingBox().getCenter();
        level().addParticle(new DustParticleOptions(color, 1.0f), pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(rgbToVector(getBulletColor().rgb()), 1.2f),
                    x, y, z, 10, 0.08D, 0.08D, 0.08D, 0.01D);
        }
    }

    private static Vector3f rgbToVector(int rgb) {
        return new Vector3f(((rgb >> 16) & 0xFF) / 255.0f, ((rgb >> 8) & 0xFF) / 255.0f, (rgb & 0xFF) / 255.0f);
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 0.4f, 1.45f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.DANMAKU_MEISTER.get() : spell;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BulletColor", getBulletColor().ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BulletColor")) {
            setBulletColor(BulletColor.byId(tag.getInt("BulletColor")));
        }
    }
}
