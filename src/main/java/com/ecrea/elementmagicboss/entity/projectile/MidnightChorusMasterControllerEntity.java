package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.MidnightChorusMasterSpell;
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
 * YH MystiaItemSpell の忠実な移植。
 * dt=2ごとにスイープ射撃。各弾は4段階の動き:
 *   Phase1: 遅延静止 (j+1 tick, 弾ごとにずれる)
 *   Phase2: 初速 speed から減速して停止 (t0 tick)
 *   Phase3: 完全停止 (t1 tick)
 *   Phase4: 再加速して飛び出す (20 tick)
 */
public class MidnightChorusMasterControllerEntity extends Entity {
    private UUID ownerUUID;
    private float damage;
    private Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);
    private int rotation = 1;

    // YH のパラメータ
    private static final int SWEEP_N = 15;     // スイープ回数
    private static final int SWEEP_DT = 2;     // スイープ間隔
    private static final int DY = 3;           // ピッチ方向の層数
    private static final int BULLETS_PER_LAYER = 10; // 各層の弾数
    private static final double SPEED = 1.6D;

    public MidnightChorusMasterControllerEntity(EntityType<? extends MidnightChorusMasterControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public MidnightChorusMasterControllerEntity(Level level, LivingEntity owner, float damage, int rotation) {
        this(ModEntities.MIDNIGHT_CHORUS_MASTER_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
        this.rotation = rotation;
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
            if (tickCount > SWEEP_N * SWEEP_DT + 60) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        if (owner == null) {
            discard();
            return;
        }
        // スイープ射撃
        int sweepTick = tickCount - 1;
        if (sweepTick >= 0 && sweepTick < SWEEP_N * SWEEP_DT && sweepTick % SWEEP_DT == 0) {
            spawnSweep(owner, sweepTick / SWEEP_DT);
        }
        if (tickCount > SWEEP_N * SWEEP_DT + 50) {
            discard();
        }
    }

    /**
     * YH MystiaItemSpell.SweepLarge.tick() の忠実な移植。
     * 1回のスイープでDY層 × BULLETS_PER_LAYER発の弾を放つ。
     * 各弾は遅延→減速→停止→再加速の4段階。
     */
    private void spawnSweep(LivingEntity owner, int sweepIndex) {
        TouhouPatternHelper.Basis basis = TouhouPatternHelper.basis(forward);
        Vec3 center = owner.position().add(0.0D, owner.getEyeHeight() - 0.15D, 0.0D);

        double yawIndex = rotation * (sweepIndex - (SWEEP_N - 0.5D) / 2.0D);

        for (int layer = 0; layer < DY; layer++) {
            double pitch = (layer - (DY - 0.5D) / 2.0D) * 10.0D;
            Vec3 dir = TouhouPatternHelper.rotate(basis, yawIndex * 10.0D, pitch);

            for (int j = 0; j < BULLETS_PER_LAYER; j++) {
                int delayTicks = j + 1;             // Phase1: 遅延静止
                int t0 = 15 - j / 2;               // Phase2: 減速持続
                int t1 = 30 - j - t0;               // Phase3: 停止持続

                // Phase2: 初速 speed、加速度 -speed/t0 で減速
                Vec3 vel = dir.scale(SPEED);
                Vec3 decel = dir.scale(-SPEED / t0);

                // Phase2 の終了位置 = center + v*t0 * 0.5 (等加速度運動)
                // Phase4: 停止位置から再加速
                Vec3 reAccel = dir.scale(SPEED / 10.0D);

                TouhouPresetBulletProjectile p = new TouhouPresetBulletProjectile(level(), owner);
                p.setPos(center.x, center.y, center.z);
                p.setDamage(damage);
                p.setDamageSpellId(MidnightChorusMasterSpell.SPELL_ID.toString());
                p.setBulletType(TouhouPresetBulletProjectile.BulletType.MENTOS);
                p.setBulletColor(TouhouPresetBulletProjectile.BulletColor.LIME);
                p.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);

                // Phase1: 遅延静止
                p.addMotionPhase(MotionPhase.wait(delayTicks));
                // Phase2: 減速 (速度 speed → 0)
                p.addMotionPhase(new MotionPhase(t0, vel, decel));
                // Phase3: 完全停止
                p.addMotionPhase(MotionPhase.wait(t1));
                // Phase4: 再加速して飛び出す
                p.addMotionPhase(new MotionPhase(20, Vec3.ZERO, reAccel));
                // Phase5: 最終速度で直進
                Vec3 finalVel = reAccel.scale(20);
                p.addMotionPhase(MotionPhase.linear(40, finalVel));

                int totalLife = delayTicks + t0 + t1 + 20 + 40;
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
        rotation = tag.getInt("Rotation");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("Damage", damage);
        tag.putDouble("ForwardX", forward.x);
        tag.putDouble("ForwardY", forward.y);
        tag.putDouble("ForwardZ", forward.z);
        tag.putInt("Rotation", rotation);
    }
}
