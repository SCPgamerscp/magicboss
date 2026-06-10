package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

public class SlimeBlockProjectile extends Projectile {

    private float damage = 10.0f;

    public SlimeBlockProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public SlimeBlockProjectile(Level level, LivingEntity owner, float damage) {
        super(ModEntities.SLIME_BLOCK_PROJECTILE.get(), level);
        this.damage = damage;
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
    }

    public void shoot(Vec3 direction, float speed) {
        Vec3 vel = direction.normalize().scale(speed);
        this.setDeltaMovement(vel);
        this.hasImpulse = true;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 vel = this.getDeltaMovement();
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitSlimeTarget);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
            if (isRemoved()) {
                return;
            }
        }

        Entity closeTarget = findOverlappingTarget();
        if (closeTarget != null) {
            onHitEntity(new EntityHitResult(closeTarget));
            return;
        }

        this.setPos(getX() + vel.x, getY() + vel.y, getZ() + vel.z);
        this.setDeltaMovement(vel.x, vel.y - 0.05, vel.z);
    }

    private boolean canHitSlimeTarget(Entity target) {
        return target != getOwner() && target.isAlive() && target.isPickable();
    }

    private Entity findOverlappingTarget() {
        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Entity target : level().getEntities(this, getBoundingBox().inflate(0.35), this::canHitSlimeTarget)) {
            double distance = distanceToSqr(target);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = target;
            }
        }
        return closest;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        Entity owner  = getOwner();
        if (target == owner) return;

        target.hurt(buildDamageSource(owner), damage);
        if (target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3600, 4, false, true, true));
            living.addEffect(new MobEffectInstance(MobEffects.POISON,            3600, 4, false, true, true));
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        discard();
    }

    private DamageSource buildDamageSource(Entity owner) {
        // Use magic damage type; owner as entity cause
        DamageSource base = level().damageSources().magic();
        return new DamageSource(base.typeHolder(), this, owner) {
            @Override
            public Component getLocalizedDeathMessage(LivingEntity killed) {
                Component ownerName = owner != null
                        ? owner.getDisplayName()
                        : Component.literal("Unknown");
                return Component.translatable("death.attack.slime_block",
                        killed.getDisplayName(), ownerName);
            }
        };
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { damage = tag.getFloat("Damage"); }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { tag.putFloat("Damage", damage); }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
