package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ClownpieceSpell;
import com.ecrea.elementmagicboss.util.ClownpieceOrientation;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * クラウンピーススペルのフィールドエンティティ。
 * YH の ClownItemSpell を YH 依存なしで再実装。
 * Laser/Spread の各 Ticker を内部クラスとして持つ。
 */
public class ClownpieceFieldEntity extends Entity {

    public static final int DUR   = 60;   // 1フェーズの長さ
    public static final int TOTAL = 120;  // 2フェーズ分

    private UUID ownerUUID;
    @Nullable private LivingEntity cachedOwner;
    private Vec3 spellDir = new Vec3(0, 0, 1);
    public Vec3 targetPos = null;
    private float damage = 6f;

    // Ticker リスト (Laser/Spread)
    private final List<LaserTicker>  lasers  = new ArrayList<>();
    private final List<SpreadTicker> spreads = new ArrayList<>();

    public ClownpieceFieldEntity(EntityType<? extends ClownpieceFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public ClownpieceFieldEntity(Level level, LivingEntity owner, float damage,
                                  Vec3 dir, @Nullable Vec3 targetPos) {
        this(ModEntities.CLOWNPIECE_FIELD.get(), level);
        this.ownerUUID   = owner.getUUID();
        this.cachedOwner = owner;
        this.spellDir    = dir;
        this.targetPos   = targetPos;
        this.damage      = damage;
    }

    @Override protected void defineSynchedData() {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    // ------------------------------------------------------------------ tick
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (tickCount > TOTAL) { discard(); return; }

        LivingEntity owner = getOwner();
        if (owner != null) {
            setPos(owner.getX(), owner.getY() + owner.getEyeHeight() * 0.5, owner.getZ());
        }

        // 向き: ターゲットがあればそちらへ、なければ spellDir
        Vec3 dir = getForward(owner);

        // フェーズ開始 (tick==1 と tick==DUR+1)
        if (tickCount == 1) {
            initPhase(0, dir, owner);
        } else if (tickCount == DUR + 1) {
            lasers.clear();
            spreads.clear();
            initPhase(1, dir, owner);
        }

        // Laser tickers
        lasers.removeIf(t -> t.tick(this, dir));
        // Spread tickers
        spreads.removeIf(t -> t.tick(this, dir));
    }

    // ---------------------------------------------------------------- initPhase
    private void initPhase(int kind, Vec3 dir, @Nullable LivingEntity owner) {
        var rand = level().random;
        if (kind == 0) {
            // Phase0: 3対のレーザー (asNormal回転ベース)
            int sign = rand.nextBoolean() ? 1 : -1;
            var nor  = ClownpieceOrientation.from(dir).asNormal();
            double ver = rand.nextDouble() * 20;
            for (int i = 0; i < 3; i++) {
                Vec3 n0 = nor.rotateDegrees(ver * sign);
                Vec3 n1 = nor.rotateDegrees(-ver * sign);
                lasers.add(new LaserTicker(dir, n0, 20, 1,  kind, i * -10));
                lasers.add(new LaserTicker(dir, n1, 20, -1, kind, i * -10));
                ver += 30;
            }
        } else {
            // Phase1: 1対のワイドレーザー
            double ver = (rand.nextDouble() * 2 - 1) * 60;
            var nor = ClownpieceOrientation.from(dir).asNormal();
            Vec3 n0 = nor.rotateDegrees(ver);
            Vec3 n1 = nor.rotateDegrees(-ver);
            lasers.add(new LaserTicker(dir, n0, 20, 1,  kind, 0));
            lasers.add(new LaserTicker(dir, n1, 20, -1, kind, 0));
        }

        // Spread tickers: 10tickごと × 4回
        int base = (kind == 0) ? 0 : DUR;
        for (int round = 0; round < 4; round++) {
            int startTick = base + round * 10;
            spreads.add(new SpreadTicker(kind, dir, round % 2 == 0 ? 9 : -9, startTick));
        }
    }

    // ---------------------------------------------------------------- helpers
    private Vec3 getForward(@Nullable LivingEntity owner) {
        if (targetPos != null) {
            Vec3 diff = targetPos.subtract(position());
            if (diff.lengthSqr() > 1e-4) return diff.normalize();
        }
        return spellDir;
    }

    public Vec3 center() { return position(); }

    /** ClownpieceBulletEntity を発射する共通メソッド */
    public void shoot(Vec3 pos, Vec3 velocity, float dmg, int bulletType) {
        LivingEntity owner = getOwner();
        int life = (int)(80 / Math.max(0.1, velocity.length()) * 1.2);
        ClownpieceBulletEntity bullet = new ClownpieceBulletEntity(level(), owner, dmg, life, bulletType);
        bullet.setPos(pos.x, pos.y, pos.z);
        bullet.setDeltaMovement(velocity);
        level().addFreshEntity(bullet);
    }

    /** Laser弾 (afterExpiryでレーザーを生成) */
    public void shootLaser(Vec3 pos, Vec3 velocity, float dmg, int bulletType,
                            int t, int dur,
                            ClownpieceOrientation orientation, double forward, double ver,
                            boolean homing, Vec3 tgtPos) {
        LivingEntity owner = getOwner();
        int life = 5 + t * 2;
        ClownpieceBulletEntity bullet = new ClownpieceBulletEntity(level(), owner, dmg, life, bulletType);
        bullet.setPos(pos.x, pos.y, pos.z);
        bullet.setDeltaMovement(velocity);
        if (homing) {
            bullet.withHomingTrail(tgtPos, bulletType, dmg);
        } else {
            Vec3 laserDir = orientation.rotateDegrees(forward, ver).normalize();
            bullet.withSpreadTrail(laserDir, bulletType, dmg);
        }
        level().addFreshEntity(bullet);
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

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner"))  ownerUUID = tag.getUUID("Owner");
        damage   = tag.getFloat("Damage");
        spellDir = new Vec3(tag.getDouble("DX"), tag.getDouble("DY"), tag.getDouble("DZ"));
        if (tag.contains("TX")) targetPos = new Vec3(tag.getDouble("TX"), tag.getDouble("TY"), tag.getDouble("TZ"));
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putFloat("Damage", damage);
        tag.putDouble("DX", spellDir.x); tag.putDouble("DY", spellDir.y); tag.putDouble("DZ", spellDir.z);
        if (targetPos != null) {
            tag.putDouble("TX", targetPos.x); tag.putDouble("TY", targetPos.y); tag.putDouble("TZ", targetPos.z);
        }
    }

    // ================================================================ Laser Ticker
    // YH ClownItemSpell.Laser を再実装
    static class LaserTicker {
        private Vec3 dir, nor;
        private final int dur, s, type;
        private int tick;

        LaserTicker(Vec3 dir, Vec3 nor, int dur, int s, int type, int startTick) {
            this.dir  = dir; this.nor = nor;
            this.dur  = dur; this.s = s; this.type = type;
            this.tick = startTick;
        }

        /** @return true = 終了 */
        boolean tick(ClownpieceFieldEntity field, Vec3 currentDir) {
            if (tick == 0) {
                this.dir = currentDir;
            }
            if (tick > 0) {
                double angle   = (45 + (1.0 * tick / dur) * 180) * s;
                var o          = ClownpieceOrientation.from(dir, nor);
                Vec3 shootDir  = o.rotateDegrees(angle).normalize();
                int bt = type == 0 ? 0 : 1;
                double fwd = (-45 + (1.0 * tick / dur) * 90) * s;
                double vrt = (-15 + (1.0 * tick / dur) * 30) * s;
                boolean homing = (type == 1);
                field.shootLaser(field.center(), shootDir.scale(0.5), field.damage, bt,
                        tick, dur, o, fwd, vrt, homing, field.targetPos);
            }
            tick++;
            return tick > dur;
        }
    }

    // ================================================================ Spread Ticker
    // YH ClownItemSpell.Spread を再実装
    // 10tick間、毎tick 3列×5行=15発を扇形に発射
    static class SpreadTicker {
        private final int kind;
        private final Vec3 baseDir;
        private final double w;
        private final int startAbsTick;  // 絶対tickのいつから始まるか
        private int localTick = 0;
        private static final int DURATION = 10;

        SpreadTicker(int kind, Vec3 baseDir, double w, int startAbsTick) {
            this.kind = kind; this.baseDir = baseDir;
            this.w = w; this.startAbsTick = startAbsTick;
        }

        boolean tick(ClownpieceFieldEntity field, Vec3 currentDir) {
            // まだ開始していない場合はスキップ
            if (field.tickCount <= startAbsTick) return false;

            var rand = field.level().random;
            var o = ClownpieceOrientation.from(baseDir);
            int n = 3;
            Vec3 cen = field.center();

            for (int i = 0; i < n; i++) {
                for (int j = -2; j <= 2; j++) {
                    double hor = (localTick - DURATION / 2.0 + (double) i / n + rand.nextDouble() / n) * w;
                    double ver = rand.nextInt(-3, 3) + j * 15;
                    Vec3 dir   = o.rotateDegrees(hor, ver).normalize();
                    float spd  = 0.8f;
                    Vec3 vel   = dir.scale(spd);
                    Vec3 pos   = cen.add(vel.scale((double) i / n));
                    // bulletType: phase0=star_red(2), phase1=spark_blue(3)
                    int bt = kind == 0 ? 2 : 3;
                    field.shoot(pos, vel, field.damage, bt);
                }
            }
            localTick++;
            return localTick >= DURATION;
        }
    }
}
