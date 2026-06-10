package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.YoukaiPolygraphSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * YH KoishiItemSpell の忠実な移植。
 * 8本のレーザーをプレイヤー中心で回転しながら照射。
 *
 * YH の方向計算:
 *   o0 = getOrientation(new Vec3(0, 0, 1)) ← Z軸基準、プレイヤーの向き非依存
 *   forward = RayTraceUtil.getRayTerm(ZERO, xRot, yRot, 1) ← プレイヤーの視線
 *   ver = atan2(forward.y, forward.horizontalDistance()) ← プレイヤーのピッチのみ使用
 *   dir = o0.rotate(angle * tick + 2π/n * i, ver)
 *
 * → レーザーの回転面はワールドのXZ平面基準で、プレイヤーのYaw は無視。
 *   プレイヤーのPitch のみ反映される。
 */
public class YoukaiPolygraphControllerEntity extends Entity {
    private UUID ownerUUID;
    private float damage;

    private static final int N = 8;
    private static final int STEP = 40;
    private static final int LASER_LIFE = 4;
    private static final float LASER_LENGTH = 40.0f;

    public YoukaiPolygraphControllerEntity(EntityType<? extends YoukaiPolygraphControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public YoukaiPolygraphControllerEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.YOUKAI_POLYGRAPH_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
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
            if (tickCount > STEP + 10) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        if (owner == null) {
            discard();
            return;
        }

        int t = tickCount - 1;
        if (t >= 0 && t <= STEP) {
            spawnLasers(owner, t);
        }
        if (tickCount > STEP + 5) {
            discard();
        }
    }

    /**
     * YH KoishiItemSpell.Lasers.tick() の忠実な移植。
     *
     * 基準座標系は (0,0,1) ベースの固定座標系。
     * プレイヤーの yaw は使わず、pitch (ver) だけ使う。
     *
     * DanmakuHelper.getOrientation(new Vec3(0,0,1)):
     *   forward = (0,0,1), normal = (0,1,0), side = (1,0,0)
     *   → .rotate(angle, ver) = side * sin(angle) * cos(ver)
     *                          + forward * cos(angle) * cos(ver)
     *                          + normal * sin(ver)
     */
    private void spawnLasers(LivingEntity owner, int t) {
        // プレイヤーの位置（追従）
        Vec3 origin = owner.position().add(0.0D, owner.getEyeHeight() - 0.1D, 0.0D);

        // プレイヤーの pitch のみ使用
        Vec3 lookDir = owner.getLookAngle();
        double ver = Math.atan2(lookDir.y, lookDir.horizontalDistance());

        // 回転角速度: 2π / N / STEP ラジアン/tick
        double anglePerTick = Math.PI * 2.0D / N / STEP;
        double cosVer = Math.cos(ver);
        double sinVer = Math.sin(ver);

        for (int i = 0; i < N; i++) {
            double baseAngle = Math.PI * 2.0D / N * i;
            double currentAngle = baseAngle + anglePerTick * t;

            // o0.rotate(currentAngle, ver) の計算
            // o0 = getOrientation(0,0,1) → forward=(0,0,1), normal=(0,1,0), side=(1,0,0)
            // rotate(angle, ver) = side*sin(angle)*cos(ver) + forward*cos(angle)*cos(ver) + normal*sin(ver)
            double sinA = Math.sin(currentAngle);
            double cosA = Math.cos(currentAngle);

            Vec3 dir = new Vec3(
                    sinA * cosVer,  // side(1,0,0) * sin(angle) * cos(ver)
                    sinVer,          // normal(0,1,0) * sin(ver)
                    cosA * cosVer    // forward(0,0,1) * cos(angle) * cos(ver)
            );
            dir = TouhouPatternHelper.safeNormalize(dir, new Vec3(0, 0, 1));

            TouhouLaserEntity laser = new TouhouLaserEntity(level(), owner, origin, dir, damage,
                    LASER_LENGTH, LASER_LIFE, 2,
                    i % 2 == 0 ? TouhouLaserEntity.LaserColor.RED : TouhouLaserEntity.LaserColor.BLUE,
                    YoukaiPolygraphSpell.SPELL_ID.toString());
            laser.setRollSeed((float) (i / (double) N));
            laser.addTag(TouhouLaserEntity.IFRAME_TAG);
            level().addFreshEntity(laser);
        }
    }

    @Nullable
    private LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        damage = tag.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("Damage", damage);
    }
}
