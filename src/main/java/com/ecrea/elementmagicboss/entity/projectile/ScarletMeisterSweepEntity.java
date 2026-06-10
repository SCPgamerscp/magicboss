package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import com.ecrea.elementmagicboss.spell.ScarletMeisterSpell;
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

public class ScarletMeisterSweepEntity extends Entity {
    private static final double INITIAL_ANGLE = -90.0D;
    private static final double HORIZONTAL_SPREAD = 15.0D;
    private static final double VERTICAL_SPREAD = 8.0D;
    private static final double LOW_SPEED = 1.6D;
    private static final double HIGH_SPEED = 2.4D;
    private static final int RANGE = 80;

    private UUID ownerUUID;
    private float projectileDamage;
    private Vec3 direction = new Vec3(0.0D, 0.0D, 1.0D);
    private Vec3 right = new Vec3(1.0D, 0.0D, 0.0D);
    private Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);

    public ScarletMeisterSweepEntity(EntityType<? extends ScarletMeisterSweepEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public ScarletMeisterSweepEntity(Level level, LivingEntity owner, float projectileDamage) {
        this(ModEntities.SCARLET_MEISTER_SWEEP.get(), level);
        this.ownerUUID = owner.getUUID();
        this.projectileDamage = projectileDamage;
        setDirection(owner.getLookAngle());
    }

    private void setDirection(Vec3 look) {
        this.direction = look.normalize();
        Vec3 worldUp = new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 computedRight = worldUp.cross(this.direction);
        if (computedRight.lengthSqr() < 1.0E-4D) {
            computedRight = new Vec3(0.0D, 0.0D, 1.0D).cross(this.direction);
        }
        this.right = computedRight.normalize();
        this.up = this.direction.cross(this.right).normalize();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            if (tickCount >= ScarletMeisterSpell.DURATION_TICKS) {
                discard();
            }
            return;
        }

        int sweepTick = tickCount - 1;
        if (sweepTick >= 0 && sweepTick < ScarletMeisterSpell.DURATION_TICKS) {
            spawnSweep(sweepTick);
        }
        if (tickCount >= ScarletMeisterSpell.DURATION_TICKS) {
            discard();
        }
    }

    private void spawnSweep(int sweepTick) {
        RandomSource random = level().getRandom();
        LivingEntity owner = getOwnerEntity();
        Entity damageOwner = owner != null ? owner : this;
        Vec3 origin = owner != null
                ? owner.position().add(0.0D, owner.getEyeHeight() - 0.15D, 0.0D)
                : position();
        double baseAngle = INITIAL_ANGLE + (360.0D / ScarletMeisterSpell.DURATION_TICKS) * sweepTick;

        for (int i = 0; i < ScarletMeisterSpell.SWEEP_COUNT; i++) {
            double horizontal = baseAngle + HORIZONTAL_SPREAD * (random.nextDouble() * 2.0D - 1.0D);
            double vertical = VERTICAL_SPREAD * random.nextGaussian();
            double speed = LOW_SPEED + (HIGH_SPEED - LOW_SPEED) * random.nextDouble();
            int lifetime = (int) Math.ceil(RANGE / speed * (1.0D + random.nextDouble() * 0.1D));
            Vec3 shotDirection = rotateDegrees(horizontal, vertical);

            spawnProjectile(origin, damageOwner, ScarletMeisterProjectile.BulletType.BUBBLE,
                    shotDirection.scale(speed), lifetime);

            double mid = 0.6D + 0.3D * random.nextDouble();
            spawnProjectile(origin, damageOwner, ScarletMeisterProjectile.BulletType.MENTOS,
                    shotDirection.scale(speed * mid), (int) (lifetime / mid * 0.8D));

            double low = 0.3D + 0.3D * random.nextDouble();
            spawnProjectile(origin, damageOwner, ScarletMeisterProjectile.BulletType.BALL,
                    shotDirection.scale(speed * low), (int) (lifetime / low * 0.6D));
        }
    }

    private void spawnProjectile(Vec3 origin, Entity owner, ScarletMeisterProjectile.BulletType bulletType,
                                 Vec3 velocity, int lifetime) {
        ScarletMeisterProjectile projectile = new ScarletMeisterProjectile(level(), owner);
        projectile.setPos(origin.x, origin.y, origin.z);
        projectile.setDamage(projectileDamage);
        projectile.setDamageSpellId(ModSpells.SCARLET_MEISTER.getId().toString());
        projectile.setBulletType(bulletType);
        projectile.setMaxLifetimeTicksCustom(lifetime);
        projectile.setDeltaMovement(velocity);
        projectile.addTag(ScarletMeisterSpell.SCARLET_MEISTER_PROJECTILE_TAG);
        level().addFreshEntity(projectile);
    }

    private Vec3 rotateDegrees(double horizontalDegrees, double verticalDegrees) {
        Vec3 yawed = rotateAroundAxis(direction, up, Math.toRadians(horizontalDegrees)).normalize();
        Vec3 yawedRight = up.cross(yawed);
        if (yawedRight.lengthSqr() < 1.0E-4D) {
            yawedRight = right;
        }
        return rotateAroundAxis(yawed, yawedRight.normalize(), -Math.toRadians(verticalDegrees)).normalize();
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
        projectileDamage = tag.getFloat("ProjectileDamage");
        direction = new Vec3(tag.getDouble("DirectionX"), tag.getDouble("DirectionY"), tag.getDouble("DirectionZ"));
        right = new Vec3(tag.getDouble("RightX"), tag.getDouble("RightY"), tag.getDouble("RightZ"));
        up = new Vec3(tag.getDouble("UpX"), tag.getDouble("UpY"), tag.getDouble("UpZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("ProjectileDamage", projectileDamage);
        tag.putDouble("DirectionX", direction.x);
        tag.putDouble("DirectionY", direction.y);
        tag.putDouble("DirectionZ", direction.z);
        tag.putDouble("RightX", right.x);
        tag.putDouble("RightY", right.y);
        tag.putDouble("RightZ", right.z);
        tag.putDouble("UpX", up.x);
        tag.putDouble("UpY", up.y);
        tag.putDouble("UpZ", up.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }
}