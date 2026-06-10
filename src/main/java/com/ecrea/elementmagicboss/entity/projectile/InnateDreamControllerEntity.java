package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.InnateDreamSpell;
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
 * YH ReimuItemSpell の忠実な移植。
 * 3つのリングが20tick間隔で発動。
 * リングはターゲット方向に直交する平面上に展開。
 *
 * Phase1 (t0=20): 詠唱者の位置から円周上に膨張 (LIGHT_GRAY)
 * Phase2 (t1=20): 円周の位置からターゲットに向かって収縮 (PURPLE)
 * Phase3 (t2+dt): 収縮完了位置からターゲットへ定速追尾 (RED)
 */
public class InnateDreamControllerEntity extends Entity {
    private UUID ownerUUID;
    private UUID targetUUID;
    private float damage;

    // YH ReimuItemSpell と同じパラメータ
    private static final int N = 20;
    private static final int R0 = 8;
    private static final int R1 = 6;
    private static final int T0 = 20;
    private static final int T1 = 20;
    private static final int T2 = 40;
    private static final int DT = 20;
    private static final double TERM_SPEED = 1.0D;

    // キャプチャされた方向情報（start() 時に固定）
    private Vec3 capturedInit;   // リング平面の基準軸 (YH: init)
    private Vec3 capturedNormal; // リング平面の法線方向 (YH: normal)
    private Vec3 capturedPos;    // 詠唱者の位置 (YH: pos)

    public InnateDreamControllerEntity(EntityType<? extends InnateDreamControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public InnateDreamControllerEntity(Level level, LivingEntity owner, LivingEntity target, float damage) {
        this(ModEntities.INNATE_DREAM_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.damage = damage;
        this.setPos(target.getX(), target.getY(), target.getZ());

        // 水平面（XZ平面）上にリングを展開（真上から見て平面になる）
        this.capturedInit = new Vec3(1.0D, 0.0D, 0.0D);   // X軸方向
        this.capturedNormal = new Vec3(0.0D, 0.0D, 1.0D);  // Z軸方向
        Vec3 ownerCenter = owner.position().add(0, owner.getEyeHeight() * 0.5D, 0);
        this.capturedPos = ownerCenter;
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
            if (tickCount > 100) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        LivingEntity target = getTargetEntity();
        if (owner == null || target == null || !target.isAlive()) {
            discard();
            return;
        }

        // 3つのリングを20tick間隔で発動（各リングに異なる色）
        if (tickCount == 1) {
            spawnRing(target, TouhouPresetBulletProjectile.BulletColor.RED);
        } else if (tickCount == 21) {
            spawnRing(target, TouhouPresetBulletProjectile.BulletColor.PURPLE);
        } else if (tickCount == 41) {
            spawnRing(target, TouhouPresetBulletProjectile.BulletColor.YELLOW);
        } else if (tickCount > 95) {
            discard();
        }
    }

    /**
     * YH ReimuItemSpell.StateChange.step() の忠実な移植。
     * 1リング = 3フェーズ分の弾を MotionPhase で制御。
     */
    private void spawnRing(LivingEntity target, TouhouPresetBulletProjectile.BulletColor color) {
        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5D, 0);
        Vec3 pos = capturedPos;

        // リング平面上の方向計算
        // YH: o0 = getOrientation(init, normal)
        // o0.rotateDegrees(angle) = normal * sin(angle) + init * cos(angle)
        // （init と normal はリング平面上の直交する2方向）

        // Phase 1: 膨張 (center → 半径 R0)
        double acc = R0 * 2.0D / (T0 * T0);
        for (int i = 0; i < N; i++) {
            double angleDeg = 360.0D / N * i;
            double angleRad = Math.toRadians(angleDeg);
            // リング平面上の方向
            Vec3 front = capturedNormal.scale(Math.sin(angleRad)).add(capturedInit.scale(Math.cos(angleRad)));

            Vec3 vel = front.scale(acc * T0);   // 初速（外向き）
            Vec3 decel = front.scale(-acc);      // 減速

            // Phase1 終了位置
            Vec3 phase1End = pos.add(vel.scale(T0)).add(decel.scale(0.5D * T0 * T0));

            // Phase2: ターゲット方向へ収縮
            Vec3 toTarget = TouhouPatternHelper.safeNormalize(targetPos.subtract(phase1End), front);
            double acc2 = R1 * 2.0D / (T1 * T1);
            Vec3 vel2 = toTarget.scale(acc2 * T1);
            Vec3 decel2 = toTarget.scale(-acc2);

            // Phase2 終了位置
            Vec3 phase2End = phase1End.add(vel2.scale(T1)).add(decel2.scale(0.5D * T1 * T1));

            // Phase3: ターゲットへ定速追尾
            Vec3 toTarget2 = TouhouPatternHelper.safeNormalize(targetPos.subtract(phase2End),
                    TouhouPatternHelper.safeNormalize(targetPos.subtract(pos), new Vec3(0, 0, 1)));
            int life = T2 + random.nextInt(DT);

            TouhouPresetBulletProjectile p = createBullet(color, T0 + T1 + life + 5);
            p.setPos(pos.x, pos.y, pos.z);

            // Phase1: 膨張（絶対位置モード）
            p.addMotionPhase(MotionPhase.rect(T0, pos, vel, decel));
            // Phase2: 収縮（絶対位置モード）
            p.addMotionPhase(MotionPhase.rect(T1, phase1End, vel2, decel2));
            // Phase3: 定速（絶対位置モード）
            p.addMotionPhase(MotionPhase.rect(life, phase2End, toTarget2.scale(TERM_SPEED), Vec3.ZERO));

            p.setMaxLifetimeTicksCustom(T0 + T1 + life + 5);
            p.setDeltaMovement(vel);
            level().addFreshEntity(p);
        }
    }

    private TouhouPresetBulletProjectile createBullet(TouhouPresetBulletProjectile.BulletColor color, int life) {
        LivingEntity owner = getOwnerEntity();
        TouhouPresetBulletProjectile p = new TouhouPresetBulletProjectile(level(), owner);
        p.setDamage(damage);
        p.setDamageSpellId(InnateDreamSpell.SPELL_ID.toString());
        p.setBulletType(TouhouPresetBulletProjectile.BulletType.CIRCLE);
        p.setBulletColor(color);
        p.setMaxLifetimeTicksCustom(life);
        p.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);
        return p;
    }

    @Nullable
    private LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Nullable
    private LivingEntity getTargetEntity() {
        if (targetUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(targetUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        if (tag.hasUUID("TargetUUID")) targetUUID = tag.getUUID("TargetUUID");
        damage = tag.getFloat("Damage");
        if (tag.contains("InitX")) {
            capturedInit = new Vec3(tag.getDouble("InitX"), tag.getDouble("InitY"), tag.getDouble("InitZ"));
            capturedNormal = new Vec3(tag.getDouble("NormX"), tag.getDouble("NormY"), tag.getDouble("NormZ"));
            capturedPos = new Vec3(tag.getDouble("PosX"), tag.getDouble("PosY"), tag.getDouble("PosZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        if (targetUUID != null) tag.putUUID("TargetUUID", targetUUID);
        tag.putFloat("Damage", damage);
        if (capturedInit != null) {
            tag.putDouble("InitX", capturedInit.x);
            tag.putDouble("InitY", capturedInit.y);
            tag.putDouble("InitZ", capturedInit.z);
            tag.putDouble("NormX", capturedNormal.x);
            tag.putDouble("NormY", capturedNormal.y);
            tag.putDouble("NormZ", capturedNormal.z);
            tag.putDouble("PosX", capturedPos.x);
            tag.putDouble("PosY", capturedPos.y);
            tag.putDouble("PosZ", capturedPos.z);
        }
    }
}
