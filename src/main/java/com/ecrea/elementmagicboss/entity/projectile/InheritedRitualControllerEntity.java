package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.InheritedRitualSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * YH SanaeItemSpell の忠実な移植。
 * 3つの五芒星パターンが10tick間隔で120°ずつ回転して出現。
 * 星パターンは forward 方向に直交する平面上に展開される。
 *
 * 各弾は:
 *   Phase1 (ZeroMover): 星の描画完了まで静止
 *   Phase2 (RectMover): 外向きに加速
 *   Phase3 (addEnd): 最終速度で直進
 */
public class InheritedRitualControllerEntity extends Entity {
    private UUID ownerUUID;
    private float damage;
    private Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
    private double baseAngle;

    // YH パラメータ
    private static final int STAR_N = 20;
    private static final int STAR_M = 2;
    private static final int WAIT_T0 = 10;
    private static final double ACCEL = 0.3D;
    private static final double DIST = 4.0D;

    public InheritedRitualControllerEntity(EntityType<? extends InheritedRitualControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public InheritedRitualControllerEntity(Level level, LivingEntity owner, float damage, double baseAngle) {
        this(ModEntities.INHERITED_RITUAL_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
        this.forward = TouhouPatternHelper.safeNormalize(owner.getLookAngle(), new Vec3(0.0D, 0.0D, 1.0D));
        this.baseAngle = baseAngle;
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
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
            if (tickCount > 60) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        if (owner == null) {
            discard();
            return;
        }
        if (tickCount == 1) {
            spawnStar(owner, baseAngle, TouhouPresetBulletProjectile.BulletColor.LIME);
        } else if (tickCount == 11) {
            spawnStar(owner, baseAngle + 120.0D, TouhouPresetBulletProjectile.BulletColor.RED);
        } else if (tickCount == 21) {
            spawnStar(owner, baseAngle + 240.0D, TouhouPresetBulletProjectile.BulletColor.BLUE);
        } else if (tickCount > 55) {
            discard();
        }
    }

    /**
     * YH SanaeItemSpell.Star.tick() の忠実な移植。
     * forward に直交する平面上に五芒星パターンを展開。
     *
     * YH の getOrientation(dir) は:
     *   forward = dir
     *   ax0 = dir に直交する "上" 方向
     *   ax1 = cross(dir, ax0) = "右" 方向
     * rotateDegrees(angle) = ax1 * sin(angle) + forward * cos(angle)
     * → ただし星パターンでは forward を forward方向ではなく
     *   直交平面の基準方向として使っている。
     *
     * o.rotateDegrees(a0 + i * 360 * 2 / 5) は平面上の方向ベクトルを返す。
     */
    private void spawnStar(LivingEntity owner, double angleOffset,
                           TouhouPresetBulletProjectile.BulletColor color) {
        // 水平面（XZ平面）上に星パターンを展開（真上から見て平面になる）
        TouhouPatternHelper.Basis horizontalBasis = new TouhouPatternHelper.Basis(
                new Vec3(0.0D, 1.0D, 0.0D),  // forward = Y軸（上向き）
                new Vec3(1.0D, 0.0D, 0.0D),  // right = X軸
                new Vec3(0.0D, 0.0D, 1.0D)   // up = Z軸
        );

        // 腰の高さに展開
        Vec3 center = owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);

        // 五芒星を構成する5頂点の方向ベクトル（水平面上）
        Vec3[] starDirs = new Vec3[5];
        for (int i = 0; i < 5; i++) {
            double angleDeg = angleOffset + i * 360.0D * 2.0D / 5.0D;
            starDirs[i] = TouhouPatternHelper.ringOffset(horizontalBasis, angleDeg, 1.0D);
        }

        // 各tick で星の辺に沿って弾を配置
        for (int tick = 0; tick < STAR_N; tick++) {
            for (int j = 0; j < STAR_M; j++) {
                double tParam = tick + 1.0D * j / STAR_M;

                // YH: p0 = starDirs[current], p1 = starDirs[next]
                // lerp で辺上の位置を計算
                int segIndex = (int) (tParam / (STAR_N / 5.0D));
                if (segIndex >= 5) segIndex = 4;
                double localT = (tParam - segIndex * (STAR_N / 5.0D)) / (STAR_N / 5.0D);

                Vec3 p0 = starDirs[segIndex % 5];
                Vec3 p1 = starDirs[(segIndex + 1) % 5];
                // lerp
                Vec3 d = p0.add(p1.subtract(p0).scale(localT));

                // YH: acc = d.length() * accel
                double acc = d.length() * ACCEL;
                int t0_local = WAIT_T0;
                int life = Math.min(50, (int) ((100.0D - acc * (t0_local * t0_local * 0.5D + DIST / ACCEL)) / (acc * t0_local)));
                if (life < 5) life = 5;

                // 弾のスポーン位置: center + d * DIST
                Vec3 spawnPos = center.add(d.scale(DIST));

                // 静止時間: 星の描画完了まで + 5tick バッファ
                int waitTicks = STAR_N - tick + 5;

                TouhouPresetBulletProjectile p = new TouhouPresetBulletProjectile(level(), owner);
                p.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                p.setDamage(damage);
                p.setDamageSpellId(InheritedRitualSpell.SPELL_ID.toString());
                p.setBulletType(TouhouPresetBulletProjectile.BulletType.SPARK);
                p.setBulletColor(color);
                p.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);

                // Phase1: 静止（待機）
                p.addMotionPhase(MotionPhase.wait(waitTicks));

                // Phase2: 加速（外向き = d方向）
                Vec3 accelDir = TouhouPatternHelper.safeNormalize(d, horizontalBasis.forward());
                Vec3 accelVec = accelDir.scale(ACCEL);
                p.addMotionPhase(new MotionPhase(t0_local, Vec3.ZERO, accelVec));

                // Phase3: 最終速度で直進
                Vec3 finalVel = accelVec.scale(t0_local);
                p.addMotionPhase(MotionPhase.linear(life + 20, finalVel));

                int totalLife = waitTicks + t0_local + life + 25;
                p.setMaxLifetimeTicksCustom(totalLife);
                p.setDeltaMovement(Vec3.ZERO);
                level().addFreshEntity(p);
            }
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
        forward = new Vec3(tag.getDouble("ForwardX"), tag.getDouble("ForwardY"), tag.getDouble("ForwardZ"));
        baseAngle = tag.getDouble("BaseAngle");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("Damage", damage);
        tag.putDouble("ForwardX", forward.x);
        tag.putDouble("ForwardY", forward.y);
        tag.putDouble("ForwardZ", forward.z);
        tag.putDouble("BaseAngle", baseAngle);
    }
}
