package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.MeshOfLightAndDarknessSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class MeshOfLightAndDarknessControllerEntity extends Entity {
    private static final double HORIZONTAL_SPREAD = 30.0D;
    private static final double VERTICAL_SPREAD = 8.0D;
    private static final double BASE_SPEED = 1.0D;
    private static final double SPEED_FALLOFF = 0.5D;

    private UUID ownerUUID;
    private float damage;
    private Vec3 origin = Vec3.ZERO;
    private Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
    private Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
    private Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);

    public MeshOfLightAndDarknessControllerEntity(EntityType<? extends MeshOfLightAndDarknessControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public MeshOfLightAndDarknessControllerEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.MESH_OF_LIGHT_AND_DARKNESS_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
        this.origin = owner.position().add(0.0D, owner.getEyeHeight() - 0.15D, 0.0D);
        setDirection(owner.getLookAngle().multiply(1.0D, 0.5D, 1.0D));
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (tickCount > MeshOfLightAndDarknessSpell.DURATION_TICKS) {
                discard();
            }
            return;
        }

        if (tickCount <= 0) {
            return;
        }

        if (tickCount > MeshOfLightAndDarknessSpell.DURATION_TICKS) {
            discard();
            return;
        }

        // 術者の現在位置・方向を追従する
        LivingEntity owner = getOwnerEntity();
        if (owner != null) {
            this.origin = owner.position().add(0.0D, owner.getEyeHeight() - 0.15D, 0.0D);
            setDirection(owner.getLookAngle().multiply(1.0D, 0.5D, 1.0D));
        }

        RandomSource random = level().getRandom();
        double step = 1.0D + (tickCount - 1) * 0.5D;
        spawnLaser(rotateVertical(-45.0D), step, random.nextDouble(), MeshOfLightAndDarknessLaserEntity.LaserColor.RED);
        spawnLaser(rotateVertical(45.0D), step, random.nextDouble(), MeshOfLightAndDarknessLaserEntity.LaserColor.BLUE);

        if (tickCount == 20) {
            shootGroup(YukariLaserDanmakuProjectile.BulletColor.RED);
        } else if (tickCount == 40) {
            shootGroup(YukariLaserDanmakuProjectile.BulletColor.BLUE);
        }
    }

    private void shootGroup(YukariLaserDanmakuProjectile.BulletColor color) {
        RandomSource random = level().getRandom();
        spawnSpread(color, YukariLaserDanmakuProjectile.BulletType.BUBBLE, 5, random);
        spawnSpread(color, YukariLaserDanmakuProjectile.BulletType.MENTOS, 50, random);
    }

    private void spawnSpread(YukariLaserDanmakuProjectile.BulletColor color,
                             YukariLaserDanmakuProjectile.BulletType bulletType,
                             int count, RandomSource random) {
        for (int i = 0; i < count; i++) {
            double d0 = (random.nextDouble() * 2.0D - 1.0D) * HORIZONTAL_SPREAD * i / count;
            double d1 = (random.nextDouble() * 2.0D - 1.0D) * HORIZONTAL_SPREAD * i / count;
            double vertical = (random.nextDouble() * 2.0D - 1.0D) * VERTICAL_SPREAD * i / count;
            double speed = BASE_SPEED - SPEED_FALLOFF / count * i;
            int life = 60 + random.nextInt(20);
            Vec3 shotDirection = rotateSpread(d0, d1 + vertical).scale(speed);

            YukariLaserDanmakuProjectile projectile = new YukariLaserDanmakuProjectile(level(), getOwnerEntity());
            projectile.setPos(origin.x, origin.y, origin.z);
            projectile.setDamage(damage);
            projectile.setDamageSpellId(ModSpells.MESH_OF_LIGHT_AND_DARKNESS.getId().toString());
            projectile.setBulletType(bulletType);
            projectile.setBulletColor(color);
            projectile.setMaxLifetimeTicksCustom(life);
            projectile.setDeltaMovement(shotDirection);
            projectile.addTag(MeshOfLightAndDarknessSpell.PROJECTILE_TAG);
            level().addFreshEntity(projectile);
        }
    }

    private void spawnLaser(Vec3 direction, double step, double rollRandom, MeshOfLightAndDarknessLaserEntity.LaserColor color) {
        Vec3 spawnPos = origin.add(direction.scale(step));
        // YH: DanmakuHelper.getOrientation(dir).rotate(PI/2, r * PI * 2)
        // レーザーはdirに垂直な平面内でランダム角度に発射
        Vec3 laserDir = rotatePerpendicular(direction, rollRandom * Math.PI * 2.0);
        MeshOfLightAndDarknessLaserEntity laser = new MeshOfLightAndDarknessLaserEntity(level(), getOwnerEntity(), spawnPos, laserDir, damage, color);
        laser.setRollSeed((float) rollRandom);
        laser.addTag(MeshOfLightAndDarknessSpell.LASER_TAG);
        level().addFreshEntity(laser);
    }

    /**
     * YH DanmakuHelper.getOrientation(dir).rotate(PI/2, angle) の再現。
     * dir に垂直な平面上でangle角度の方向ベクトルを返す。
     */
    private static Vec3 rotatePerpendicular(Vec3 dir, double angle) {
        dir = dir.normalize();
        // getOrientation(dir) と同じロジック: normal と side を求める
        double val = dir.x * dir.x + dir.z * dir.z;
        Vec3 normal;
        if (val < 1e-4) {
            normal = new Vec3(1, 0, 0);
        } else {
            normal = new Vec3(-dir.x * dir.y, val, -dir.z * dir.y).normalize();
        }
        Vec3 side = dir.cross(normal).normalize();
        // rotate(PI/2, angle) = side*sin(PI/2)*cos(angle) + forward*cos(PI/2)*cos(angle) + normal*sin(angle)
        //                      = side*cos(angle) + normal*sin(angle)
        return side.scale(Math.cos(angle)).add(normal.scale(Math.sin(angle)));
    }

    private void setDirection(Vec3 look) {
        Vec3 normalized = look.normalize();
        if (normalized.lengthSqr() < 1.0E-4D) {
            normalized = new Vec3(0.0D, 0.0D, 1.0D);
        }
        this.forward = normalized;
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 computedRight = worldUp.cross(this.forward);
        if (computedRight.lengthSqr() < 1.0E-4D) {
            computedRight = new Vec3(0.0D, 0.0D, 1.0D).cross(this.forward);
        }
        this.right = computedRight.normalize();
        this.up = this.forward.cross(this.right).normalize();
    }

    private Vec3 rotateVertical(double degrees) {
        return rotateAroundAxis(forward, right, Math.toRadians(degrees)).normalize();
    }

    private Vec3 rotateSpread(double yawDegrees, double pitchDegrees) {
        Vec3 yawed = rotateAroundAxis(forward, up, Math.toRadians(yawDegrees)).normalize();
        Vec3 yawedRight = up.cross(yawed);
        if (yawedRight.lengthSqr() < 1.0E-4D) {
            yawedRight = right;
        }
        return rotateAroundAxis(yawed, yawedRight.normalize(), -Math.toRadians(pitchDegrees)).normalize();
    }

    private static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double angleRadians) {
        Vec3 normalizedAxis = axis.normalize();
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        return vector.scale(cos)
                .add(normalizedAxis.cross(vector).scale(sin))
                .add(normalizedAxis.scale(normalizedAxis.dot(vector) * (1.0D - cos)));
    }

    @Nullable
    private LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        damage = tag.getFloat("Damage");
        origin = new Vec3(tag.getDouble("OriginX"), tag.getDouble("OriginY"), tag.getDouble("OriginZ"));
        setDirection(new Vec3(tag.getDouble("ForwardX"), tag.getDouble("ForwardY"), tag.getDouble("ForwardZ")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("Damage", damage);
        tag.putDouble("OriginX", origin.x);
        tag.putDouble("OriginY", origin.y);
        tag.putDouble("OriginZ", origin.z);
        tag.putDouble("ForwardX", forward.x);
        tag.putDouble("ForwardY", forward.y);
        tag.putDouble("ForwardZ", forward.z);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }
}
