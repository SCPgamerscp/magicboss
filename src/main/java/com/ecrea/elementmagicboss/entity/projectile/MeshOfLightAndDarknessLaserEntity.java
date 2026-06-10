package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class MeshOfLightAndDarknessLaserEntity extends Entity implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(MeshOfLightAndDarknessLaserEntity.class, EntityDataSerializers.INT);
    private static final float LENGTH = 80.0f;
    private static final int LIFE = 100;

    public enum LaserColor {
        RED,
        BLUE;

        public static LaserColor byId(int id) {
            LaserColor[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    private Vec3 laserDir = new Vec3(0.0D, 0.0D, 1.0D);
    private float damage = 5.0f;
    private float rollSeed;
    private UUID ownerUUID;
    @Nullable private LivingEntity cachedOwner;

    public MeshOfLightAndDarknessLaserEntity(EntityType<? extends MeshOfLightAndDarknessLaserEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public MeshOfLightAndDarknessLaserEntity(Level level, @Nullable LivingEntity owner,
                                             Vec3 pos, Vec3 dir, float damage, LaserColor color) {
        this(ModEntities.MESH_OF_LIGHT_AND_DARKNESS_LASER.get(), level);
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
        this.laserDir = dir.normalize();
        this.damage = damage;
        this.setLaserColor(color);
        this.setPos(pos.x, pos.y, pos.z);
        syncRotationFromDir();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR, LaserColor.RED.ordinal());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount >= LIFE) {
                discard();
            }
            return;
        }

        if (tickCount >= LIFE) {
            discard();
            return;
        }

        if (tickCount % 4 == 0) {
            dealLineDamage();
        }
    }

    private void dealLineDamage() {
        Vec3 start = position();
        Vec3 end = start.add(laserDir.scale(LENGTH));
        AABB box = new AABB(start, end).inflate(0.9D);
        LivingEntity owner = getOwner();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != owner && (owner == null || !owner.isAlliedTo(entity)))) {
            double dist = distanceToLine(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), start, laserDir);
            if (dist < 1.0D) {
                target.invulnerableTime = 0;
                DamageSources.applyDamage(target, damage,
                        ModSpells.MESH_OF_LIGHT_AND_DARKNESS.get().getDamageSource(this, owner));
                // hurt() 後にバニラが invulnerableTime=20 にリセットするので、再度 0 にする
                target.invulnerableTime = 0;
            }
        }
    }

    private static double distanceToLine(Vec3 point, Vec3 lineOrigin, Vec3 lineDir) {
        Vec3 diff = point.subtract(lineOrigin);
        double t = diff.dot(lineDir);
        if (t < 0.0D || t > LENGTH) {
            return Double.MAX_VALUE;
        }
        return diff.subtract(lineDir.scale(t)).length();
    }

    public float getOpacity(float partialTick) {
        float t = tickCount + partialTick;
        if (t < 6.0F) {
            return 0.2F + t / 6.0F * 0.8F;
        }
        if (t < LIFE - 10.0F) {
            return 1.0F;
        }
        return Math.max(0.0F, (LIFE - t) / 10.0F);
    }

    public LaserColor getLaserColor() {
        return LaserColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setLaserColor(LaserColor color) {
        this.entityData.set(DATA_COLOR, color.ordinal());
    }

    public float getRollSeed() {
        return rollSeed;
    }

    public void setRollSeed(float rollSeed) {
        this.rollSeed = rollSeed;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner != null && cachedOwner.isAlive()) {
            return cachedOwner;
        }
        if (ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                cachedOwner = living;
                return living;
            }
        }
        return null;
    }

    private void syncRotationFromDir() {
        double horizontal = laserDir.horizontalDistance();
        float yRot = -(float) (Mth.atan2(laserDir.x, laserDir.z) * Mth.RAD_TO_DEG);
        float xRot = -(float) (Mth.atan2(laserDir.y, horizontal) * Mth.RAD_TO_DEG);
        this.setYRot(yRot);
        this.yRotO = yRot;
        this.setXRot(xRot);
        this.xRotO = xRot;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeDouble(laserDir.x);
        buffer.writeDouble(laserDir.y);
        buffer.writeDouble(laserDir.z);
        buffer.writeInt(getLaserColor().ordinal());
        buffer.writeFloat(damage);
        buffer.writeFloat(rollSeed);
        Entity owner = getOwner();
        buffer.writeInt(owner != null ? owner.getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        laserDir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        setLaserColor(LaserColor.byId(buffer.readInt()));
        damage = buffer.readFloat();
        rollSeed = buffer.readFloat();
        int id = buffer.readInt();
        if (id >= 0) {
            Entity entity = level().getEntity(id);
            if (entity instanceof LivingEntity living) {
                cachedOwner = living;
            }
        }
        syncRotationFromDir();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("DirX", laserDir.x);
        tag.putDouble("DirY", laserDir.y);
        tag.putDouble("DirZ", laserDir.z);
        tag.putInt("Color", getLaserColor().ordinal());
        tag.putFloat("Damage", damage);
        tag.putFloat("RollSeed", rollSeed);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        laserDir = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        setLaserColor(LaserColor.byId(tag.getInt("Color")));
        damage = tag.getFloat("Damage");
        rollSeed = tag.getFloat("RollSeed");
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        syncRotationFromDir();
    }
}
