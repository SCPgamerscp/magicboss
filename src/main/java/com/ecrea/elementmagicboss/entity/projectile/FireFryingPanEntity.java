package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.DanmakuShotSpell;
import com.ecrea.elementmagicboss.spell.FireFryingPanSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

public class FireFryingPanEntity extends Entity {
    public static final float HEIGHT_ABOVE_TARGET = 3.5f;
    private static final int LIFETIME_TICKS = FireFryingPanSpell.DURATION_TICKS;
    private static final int PROJECTILES_PER_BURST = FireFryingPanSpell.PROJECTILES_PER_BURST;
    private static final float MAX_HORIZONTAL_SPEED = 0.18f;
    private static final float MIN_UPWARD_SPEED = 0.64f;
    private static final float MAX_UPWARD_SPEED = 0.98f;
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(FireFryingPanEntity.class, EntityDataSerializers.INT);

    private UUID targetUUID;
    private UUID ownerUUID;
    private float projectileDamage;

    public FireFryingPanEntity(EntityType<? extends FireFryingPanEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public FireFryingPanEntity(Level level, LivingEntity owner, LivingEntity target, float projectileDamage) {
        this(ModEntities.FIRE_FRYING_PAN.get(), level);
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.projectileDamage = projectileDamage;
        this.entityData.set(TARGET_ID, target.getId());
        this.setPos(positionAboveTarget(target));
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(TARGET_ID, 0);
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity target = getTrackedTarget();
        if (target != null) {
            setPos(positionAboveTarget(target));
        } else if (!level().isClientSide) {
            discard();
            return;
        }

        if (!level().isClientSide) {
            if (tickCount > LIFETIME_TICKS) {
                discard();
                return;
            }
            if (tickCount % FireFryingPanSpell.BURST_INTERVAL_TICKS == 0) {
                spawnBurst();
            }
        }
    }

    private void spawnBurst() {
        LivingEntity owner = getOwnerEntity();
        LivingEntity target = getTrackedTarget();
        if (target == null) {
            return;
        }
        RandomSource random = level().getRandom();
        Vec3 origin = position().add(0, -0.15, 0);

        for (int i = 0; i < PROJECTILES_PER_BURST; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double horizontalSpeed = Math.pow(random.nextDouble(), 2.0D) * MAX_HORIZONTAL_SPEED;
            double upwardSpeed = MIN_UPWARD_SPEED + random.nextDouble() * (MAX_UPWARD_SPEED - MIN_UPWARD_SPEED);
            Vec3 velocity = new Vec3(
                    Math.cos(angle) * horizontalSpeed,
                    upwardSpeed,
                    Math.sin(angle) * horizontalSpeed
            );

            FireFryingPanProjectile projectile = new FireFryingPanProjectile(level(), owner);
            projectile.setPos(origin.x, origin.y, origin.z);
            projectile.setDamage(projectileDamage);
            projectile.setVariant(random.nextInt(DanmakuShotSpell.PELLET_COUNT));
            projectile.setDamageSpellId(ModSpells.FIRE_FRYING_PAN.getId().toString());
            projectile.setDeltaMovement(velocity);
            level().addFreshEntity(projectile);
        }
    }

    private static Vec3 positionAboveTarget(LivingEntity target) {
        return new Vec3(target.getX(), target.getBoundingBox().maxY + HEIGHT_ABOVE_TARGET, target.getZ());
    }

    @Nullable
    private LivingEntity getTrackedTarget() {
        Entity byId = entityData.get(TARGET_ID) == 0 ? null : level().getEntity(entityData.get(TARGET_ID));
        if (byId instanceof LivingEntity living && living.isAlive() && !living.isRemoved()) {
            return living;
        }
        if (!level().isClientSide && targetUUID != null) {
            Entity byUuid = ((ServerLevel) level()).getEntity(targetUUID);
            if (byUuid instanceof LivingEntity living && living.isAlive() && !living.isRemoved()) {
                entityData.set(TARGET_ID, living.getId());
                return living;
            }
        }
        return null;
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    public int getAnimationFrame() {
        return (tickCount / 4) & 1;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("TargetUUID")) {
            targetUUID = tag.getUUID("TargetUUID");
        }
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        projectileDamage = tag.getFloat("ProjectileDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (targetUUID != null) {
            tag.putUUID("TargetUUID", targetUUID);
        }
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("ProjectileDamage", projectileDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }
}
