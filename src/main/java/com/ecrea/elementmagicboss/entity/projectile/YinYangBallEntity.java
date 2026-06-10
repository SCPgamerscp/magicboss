package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class YinYangBallEntity extends Projectile {

    private static final double HOMING_STRENGTH = 0.35;

    private float damage = 5.0f;
    private int sealDuration = 600; // 30 seconds
    @Nullable
    private UUID homingTargetUUID;

    public YinYangBallEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public YinYangBallEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.YIN_YANG_BALL.get(), level);
        this.damage = damage;
        this.setOwner(owner);
        this.setNoGravity(true);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public void shoot(Vec3 direction, float speed) {
        Vec3 vel = direction.normalize().scale(speed);
        this.setDeltaMovement(vel);
        this.hasImpulse = true;
    }

    public void setHomingTarget(@Nullable LivingEntity target) {
        this.homingTargetUUID = target == null ? null : target.getUUID();
    }

    @Override
    public void tick() {
        super.tick();
        updateHomingMotion();

        Vec3 vel = this.getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitFantasySealTarget);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            if (isRemoved()) {
                return;
            }
        }

        this.setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);

        // Discard after 5 seconds if no hit
        if (this.tickCount > 100) {
            discard();
        }
    }

    private void updateHomingMotion() {
        LivingEntity target = getHomingTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 current = getDeltaMovement();
        double speed = Math.max(current.length(), 0.2);
        Vec3 toTarget = target.getBoundingBox().getCenter().subtract(position());
        if (toTarget.lengthSqr() < 0.0001) {
            return;
        }

        Vec3 desired = toTarget.normalize().scale(speed);
        Vec3 guided = current.scale(1.0 - HOMING_STRENGTH).add(desired.scale(HOMING_STRENGTH));
        if (guided.lengthSqr() > 0.0001) {
            setDeltaMovement(guided.normalize().scale(speed));
        }
    }

    @Nullable
    private LivingEntity getHomingTarget() {
        if (homingTargetUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity target = serverLevel.getEntity(homingTargetUUID);
        return target instanceof LivingEntity living ? living : null;
    }

    private boolean canHitFantasySealTarget(Entity target) {
        Entity owner = getOwner();
        return target != owner
                && target instanceof LivingEntity
                && target.isAlive()
                && target.isPickable()
                && (owner == null || !owner.isAlliedTo(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;

        Entity target = result.getEntity();
        Entity owner  = getOwner();
        if (!(target instanceof LivingEntity living)) { discard(); return; }
        if (target == owner) return;

        // Bypass invulnerability
        living.invulnerableTime = 0;

        // Damage
        DamageSources.applyDamage(living, damage,
                ModSpells.FANTASY_SEAL.get().getDamageSource(this, owner));

        // Apply Seal debuff on FIRST hit only
        if (!living.hasEffect(ModMobEffects.SEAL.get())) {
            living.addEffect(new MobEffectInstance(
                    ModMobEffects.SEAL.get(), sealDuration, 0, false, true, true));
        }

        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage       = tag.getFloat("Damage");
        sealDuration = tag.getInt("SealDuration");
        if (tag.hasUUID("HomingTargetUUID")) {
            homingTargetUUID = tag.getUUID("HomingTargetUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage);
        tag.putInt("SealDuration", sealDuration);
        if (homingTargetUUID != null) {
            tag.putUUID("HomingTargetUUID", homingTargetUUID);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
