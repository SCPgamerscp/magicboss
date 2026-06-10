package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class HeartOrbitEntity extends Entity {

    // texture index 1-6 synced to client for rendering
    private static final EntityDataAccessor<Integer> DATA_INDEX =
            SynchedEntityData.defineId(HeartOrbitEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private double baseAngle; // initial angle offset (radians)

    private static final double RADIUS = 2.0;
    // 1 round per 3 seconds = 60 ticks -> step = 2*PI/60
    private static final double SPEED  = Math.PI * 2.0 / 60.0;

    public HeartOrbitEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public HeartOrbitEntity(Level level, LivingEntity owner, int textureIndex, double baseAngle) {
        this(ModEntities.HEART_ORBIT.get(), level);
        this.ownerUUID = owner.getUUID();
        this.baseAngle = baseAngle;
        this.entityData.set(DATA_INDEX, textureIndex);
        // Initial position
        double tx = owner.getX() + Math.cos(baseAngle) * RADIUS;
        double ty = owner.getY() + owner.getBbHeight() * 0.5;
        double tz = owner.getZ() + Math.sin(baseAngle) * RADIUS;
        this.setPos(tx, ty, tz);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_INDEX, 1);
    }

    public int getTextureIndex() {
        return this.entityData.get(DATA_INDEX);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        LivingEntity owner = getOwnerEntity();
        if (owner == null || !owner.isAlive()
                || !owner.hasEffect(ModMobEffects.HEALING_DETERMINATION.get())) {
            discard();
            return;
        }

        double angle = baseAngle - tickCount * SPEED;
        double tx = owner.getX() + Math.cos(angle) * RADIUS;
        double ty = owner.getY() + owner.getBbHeight() * 0.5;
        double tz = owner.getZ() + Math.sin(angle) * RADIUS;
        setPos(tx, ty, tz);
    }

    @Nullable
    public UUID getOwnerUUID() { return ownerUUID; }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || level().isClientSide) return null;
        var e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        baseAngle = tag.getDouble("BaseAngle");
        entityData.set(DATA_INDEX, tag.getInt("TextureIndex"));
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("BaseAngle", baseAngle);
        tag.putInt("TextureIndex", entityData.get(DATA_INDEX));
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 4096.0; }
}