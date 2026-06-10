package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ClownpieceSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import io.redspace.ironsspellbooks.damage.DamageSources;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * クラウンピースレーザーエンティティ。
 * YH の ItemLaserEntity を YH 依存なしで再実装。
 * setupTime(10, 10, 60, 10) → prepare=10, start=20, end=80, life=90
 *
 * laserType:
 *   0 = blue (SpreadTrail: Phase0)
 *   1 = red  (HomingTrail: Phase1)
 */
public class ClownpieceLaserEntity extends Entity implements IEntityAdditionalSpawnData {

    // タイムライン (setupTime(10, 10, DUR=60, 10))
    public static final int PREPARE = 10;
    public static final int START   = 20;
    public static final int END     = 80;
    public static final int LIFE    = 90;

    public static final float LENGTH = 60f;

    // クライアント同期フィールド (spawnDataで送る)
    public Vec3 laserDir = new Vec3(0, 0, 1);
    public int  laserType = 0;   // 0=blue, 1=red
    private float damage = 5f;
    private UUID ownerUUID;
    @Nullable private LivingEntity cachedOwner;

    public ClownpieceLaserEntity(EntityType<? extends ClownpieceLaserEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public ClownpieceLaserEntity(Level level, @Nullable LivingEntity owner,
                                  Vec3 pos, Vec3 dir, float damage, int laserType) {
        this(ModEntities.CLOWNPIECE_LASER.get(), level);
        if (owner != null) { this.ownerUUID = owner.getUUID(); this.cachedOwner = owner; }
        this.laserDir  = dir.normalize();
        this.laserType = laserType;
        this.damage    = damage;
        this.setPos(pos.x, pos.y, pos.z);
        // rotation for rendering
        // Minecraft: dir.x = -sin(yRot), dir.z = cos(yRot) → yRot = -atan2(dir.x, dir.z)
        double horiz = laserDir.horizontalDistance();
        float yRot = -(float)(Mth.atan2(laserDir.x, laserDir.z) * Mth.RAD_TO_DEG);
        float xRot = -(float)(Mth.atan2(laserDir.y, horiz) * Mth.RAD_TO_DEG);
        this.setYRot(yRot); this.yRotO = yRot;
        this.setXRot(xRot); this.xRotO = xRot;
    }

    @Override protected void defineSynchedData() {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (tickCount >= LIFE) { discard(); return; }

        // active phase: tick START ~ END で沿線エンティティにダメージ
        if (tickCount >= START && tickCount < END && tickCount % 4 == 0) {
            dealLineDamage();
        }
    }

    private void dealLineDamage() {
        Vec3 start = position();
        Vec3 end   = start.add(laserDir.scale(LENGTH));
        // ラインAABBで近傍エンティティ取得
        AABB box = new AABB(start, end).inflate(0.8);
        LivingEntity owner = getOwner();
        for (LivingEntity le : level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != owner && (owner == null || !owner.isAlliedTo(e)))) {
            // 各エンティティがレーザーライン近くにいるか判定
            double dist = distanceToLine(le.position().add(0, le.getBbHeight() * 0.5, 0), start, laserDir);
            if (dist < 0.9) {
                le.invulnerableTime = 0;
                DamageSources.applyDamage(le, damage,
                        io.redspace.ironsspellbooks.api.registry.SpellRegistry
                                .getSpell("elementmagicboss:clownpiece")
                                .getDamageSource(this, owner));
            }
        }
    }

    private static double distanceToLine(Vec3 point, Vec3 lineOrigin, Vec3 lineDir) {
        Vec3 d = point.subtract(lineOrigin);
        double t = d.dot(lineDir);
        if (t < 0 || t > LENGTH) return Double.MAX_VALUE;
        return d.subtract(lineDir.scale(t)).length();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner != null && cachedOwner.isAlive()) return cachedOwner;
        if (ownerUUID != null && level() instanceof ServerLevel sl) {
            Entity e = sl.getEntity(ownerUUID);
            if (e instanceof LivingEntity le) { cachedOwner = le; return le; }
        }
        return null;
    }

    /** クライアントでの描画用: 現在の opacity (0..1) */
    public float getOpacity(float partialTick) {
        float t = tickCount + partialTick;
        if (t < PREPARE) return 0.1f;
        if (t < START)   return (t - PREPARE) / (float)(START - PREPARE) * 0.9f + 0.1f;
        if (t < END)     return 1f;
        if (t < LIFE)    return (LIFE - t) / (float)(LIFE - END);
        return 0f;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeDouble(laserDir.x); buf.writeDouble(laserDir.y); buf.writeDouble(laserDir.z);
        buf.writeInt(laserType);
        buf.writeFloat(damage);
        Entity owner = getOwner();
        buf.writeInt(owner != null ? owner.getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        laserDir   = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        laserType  = buf.readInt();
        damage     = buf.readFloat();
        int id = buf.readInt();
        if (id >= 0) { Entity e = level().getEntity(id); if (e != null) setOwner(e); }
        // クライアント側でも yRot/xRot を laserDir から設定
        // Minecraft: dir.x = -sin(yRot) → yRot = -atan2(dir.x, dir.z)
        double horiz = laserDir.horizontalDistance();
        float yRot = -(float)(Mth.atan2(laserDir.x, laserDir.z) * Mth.RAD_TO_DEG);
        float xRot = -(float)(Mth.atan2(laserDir.y, horiz) * Mth.RAD_TO_DEG);
        this.setYRot(yRot); this.yRotO = yRot;
        this.setXRot(xRot); this.xRotO = xRot;
    }

    private void setOwner(Entity e) { if (e instanceof LivingEntity le) cachedOwner = le; }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("LDX", laserDir.x); tag.putDouble("LDY", laserDir.y); tag.putDouble("LDZ", laserDir.z);
        tag.putInt("LType", laserType);
        tag.putFloat("Dmg", damage);
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        laserDir  = new Vec3(tag.getDouble("LDX"), tag.getDouble("LDY"), tag.getDouble("LDZ"));
        laserType = tag.getInt("LType");
        damage    = tag.getFloat("Dmg");
        if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
    }
}
