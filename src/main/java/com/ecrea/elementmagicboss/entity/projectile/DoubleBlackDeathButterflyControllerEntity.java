package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.DoubleBlackDeathButterflySpell;
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
 * YH YukariItemSpellButterfly の忠実な移植。
 * シアンとマゼンタの2群（各100発）。
 * 各弾は5段階の動き:
 *   Phase1 (t0=40): 外側へ飛び出して減速
 *   Phase2 (t1=10): 向きを変えつつ静止（ZeroMover相当）
 *   Phase3 (t2=10): 旋回しながら角速度加速
 *   Phase4 (t3=30): 等角速度で旋回
 *   Phase5 (t4=40): 旋回終了地点から直線飛行
 *
 * YHの PolarMover は極座標系の複雑な制御をするが、
 * ここでは近似的に各フェーズの速度を計算して MotionPhase で再現する。
 */
public class DoubleBlackDeathButterflyControllerEntity extends Entity {
    private UUID ownerUUID;
    private float damage;
    private Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

    // YH パラメータ
    private static final int BUTTERFLY_N = 100;    // 1群の弾数
    private static final int MRANGE = 12;          // 最大半径
    private static final int VRANGE = 8;           // 半径ランダム範囲
    private static final int T0 = 40;              // Phase1: 膨張
    private static final int T1 = 10;              // Phase2: 静止
    private static final int T2 = 10;              // Phase3: 旋回加速
    private static final int T3 = 30;              // Phase4: 等旋回
    private static final int T4 = 40;              // Phase5: 直進
    private static final double TVR = 0.8D;        // 角速度係数
    private static final double AVAR = Math.PI / 4.0D; // ピッチランダム範囲

    public DoubleBlackDeathButterflyControllerEntity(EntityType<? extends DoubleBlackDeathButterflyControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public DoubleBlackDeathButterflyControllerEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.DOUBLE_BLACK_DEATH_BUTTERFLY_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
        this.forward = TouhouPatternHelper.safeNormalize(owner.getLookAngle(), new Vec3(0.0D, 0.0D, 1.0D));
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
            if (tickCount > 6) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        if (owner == null) {
            discard();
            return;
        }
        if (tickCount == 1) {
            spawnButterflies(owner, TouhouPresetBulletProjectile.BulletColor.CYAN, 1);
            spawnButterflies(owner, TouhouPresetBulletProjectile.BulletColor.MAGENTA, -1);
        } else if (tickCount > 2) {
            discard();
        }
    }

    /**
     * YH YukariItemSpellButterfly.launchButterfly() の移植。
     * 5段階の移動パターンを MotionPhase で再現する。
     */
    private void spawnButterflies(LivingEntity owner, TouhouPresetBulletProjectile.BulletColor color, int dire) {
        var random = level().getRandom();
        TouhouPatternHelper.Basis basis = TouhouPatternHelper.basis(forward);
        Vec3 origin = owner.position().add(0.0D, owner.getEyeHeight() - 0.1D, 0.0D);

        double wvr = (TVR / MRANGE) * dire; // 角速度（YHのwvr）

        for (int i = 0; i < BUTTERFLY_N; i++) {
            double a0 = 2.0D * Math.PI / BUTTERFLY_N * i;
            double ver = (random.nextDouble() * 2.0D - 1.0D) * AVAR;

            // 放射方向ベクトル
            Vec3 a1 = TouhouPatternHelper.rotate(basis,
                    Math.toDegrees(a0), Math.toDegrees(ver));

            // 半径のランダム化
            double range = MRANGE + VRANGE * (random.nextDouble() * 2.0D - 1.0D);
            double va = range * 2.0D / (T0 * T0);  // 加速度
            double vr = va * T0;                     // 初速

            // Phase1: 膨張 - 初速 vr で外向き、加速度 -va で減速
            Vec3 vel1 = a1.scale(vr);
            Vec3 accel1 = a1.scale(-va);

            // Phase1 終了位置: origin + vel1 * T0 + 0.5 * accel1 * T0^2
            // = origin + a1 * (vr * T0 - 0.5 * va * T0^2)
            // = origin + a1 * (vr * T0 * 0.5)  (since va = 2*range/T0^2, vr=va*T0)
            // = origin + a1 * range

            // Phase2: 静止 (向き変更)
            // YH: ZeroMover で向きだけ補間。ここでは単純に静止。

            // Phase3 & Phase4: 旋回
            // YHでは PolarMover で極座標系の旋回を行う。
            // ここでは近似的に、旋回方向のベクトルを計算して円弧的な動きをする。

            // 旋回の横方向ベクトル（a1に直交する方向）
            Vec3 tangent = TouhouPatternHelper.safeNormalize(
                    basis.up().cross(a1), basis.right());
            tangent = tangent.scale(dire); // 回転方向

            // Phase3: 角速度加速（t2 tick）
            // 初期角速度 0 → 最終角速度 wvr
            // 旋回速度 = tangent * range * angular_velocity
            double angVelFinal = wvr * T2; // Phase3の終了時角速度
            Vec3 phase3Accel = tangent.scale(range * wvr / T2);

            // Phase4: 等角速度旋回（t3 tick）
            Vec3 phase4Vel = tangent.scale(range * angVelFinal);

            // Phase5: 直進
            // 旋回終了位置での接線方向に飛ぶ
            // 旋回で大きく角度が変わるので、最終的な方向を推定
            double totalAngle = angVelFinal * T3 + 0.5 * wvr * T2 * T2 / T2 * T3;
            Vec3 finalDir = TouhouPatternHelper.rotateAroundAxis(a1, basis.forward(), totalAngle * dire);
            Vec3 phase5Vel = TouhouPatternHelper.safeNormalize(finalDir, a1).scale(vr * 0.3D);

            // 弾を生成
            TouhouPresetBulletProjectile p = new TouhouPresetBulletProjectile(level(), owner);
            p.setPos(origin.x, origin.y, origin.z);
            p.setDamage(damage);
            p.setDamageSpellId(DoubleBlackDeathButterflySpell.SPELL_ID.toString());
            p.setBulletType(TouhouPresetBulletProjectile.BulletType.BUTTERFLY);
            p.setBulletColor(color);
            p.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);

            // Phase1: 膨張（減速）
            p.addMotionPhase(new MotionPhase(T0, vel1, accel1));

            // Phase2: 静止
            p.addMotionPhase(MotionPhase.wait(T1));

            // Phase3: 旋回加速
            p.addMotionPhase(new MotionPhase(T2, Vec3.ZERO, phase3Accel));

            // Phase4: 等角速度旋回
            p.addMotionPhase(MotionPhase.linear(T3, phase4Vel));

            // Phase5: 直進
            p.addMotionPhase(MotionPhase.linear(T4, phase5Vel));

            int totalLife = T0 + T1 + T2 + T3 + T4 + random.nextInt(40);
            p.setMaxLifetimeTicksCustom(totalLife);
            p.setDeltaMovement(vel1);
            level().addFreshEntity(p);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("Damage", damage);
        tag.putDouble("ForwardX", forward.x);
        tag.putDouble("ForwardY", forward.y);
        tag.putDouble("ForwardZ", forward.z);
    }
}
