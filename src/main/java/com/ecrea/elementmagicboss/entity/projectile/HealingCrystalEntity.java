package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import net.minecraftforge.network.NetworkHooks;

public class HealingCrystalEntity extends Entity {

    private static final float MAX_HEALTH = 50.0f;
    private static final int HEAL_RANGE   = 20;

    // Synced: beam target block pos for renderer
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET =
            SynchedEntityData.defineId(HealingCrystalEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    private float health = MAX_HEALTH;
    private float healAmount = 1.0f;
    private UUID ownerUUID;

    public HealingCrystalEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public HealingCrystalEntity(Level level, LivingEntity owner, float healAmount) {
        this(ModEntities.HEALING_CRYSTAL.get(), level);
        this.ownerUUID  = owner.getUUID();
        this.healAmount = healAmount;
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_BEAM_TARGET, Optional.empty());
    }

    public Optional<BlockPos> getBeamTarget() {
        return this.entityData.get(DATA_BEAM_TARGET);
    }

    public void setBeamTarget(@Nullable BlockPos pos) {
        this.entityData.set(DATA_BEAM_TARGET, Optional.ofNullable(pos));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        LivingEntity owner = getOwnerEntity();

        // Heal every second
        if (tickCount % 20 == 0 && owner != null && owner.isAlive()) {
            double dist = owner.distanceTo(this);
            if (dist <= HEAL_RANGE) {
                owner.heal(healAmount);
                // Update beam target to owner's block pos
                setBeamTarget(owner.blockPosition().above());
            } else {
                setBeamTarget(null);
            }
        }

        // Auto discard if owner gone
        if (owner == null || !owner.isAlive()) {
            discard();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;
        health -= amount;
        if (health <= 0) {
            discard();
            level().explode(this, getX(), getY(), getZ(), 1.0f,
                    Level.ExplosionInteraction.NONE);
        }
        return true;
    }

    @Override public boolean isPickable() { return true; }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || level().isClientSide) return null;
        var e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID);
        return e instanceof LivingEntity le ? le : null;
    }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }

    public float getHealth()    { return health; }
    public float getMaxHealth() { return MAX_HEALTH; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        health     = tag.getFloat("Health");
        healAmount = tag.getFloat("HealAmount");
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Health",     health);
        tag.putFloat("HealAmount", healAmount);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 65536.0; }
}
