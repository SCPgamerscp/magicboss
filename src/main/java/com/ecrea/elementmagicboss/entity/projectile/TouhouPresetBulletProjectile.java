package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TouhouPresetBulletProjectile extends DanmakuShotProjectile {
    public static final String IFRAME_TAG = "elementmagicboss_touhou_preset_iframe";

    private static final EntityDataAccessor<Integer> DATA_BULLET_TYPE =
            SynchedEntityData.defineId(TouhouPresetBulletProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BULLET_COLOR =
            SynchedEntityData.defineId(TouhouPresetBulletProjectile.class, EntityDataSerializers.INT);

    private int maxLifetimeTicks = 120;

    // ---- 多段階移動システム (YH CompositeMover相当) ----
    private final List<MotionPhase> motionPhases = new ArrayList<>();
    private int phaseIndex = 0;
    private int phaseStartAge = 0;
    /** 前回のtickで設定した速度。外部力（SureHit等）の検出に使用 */
    private Vec3 lastSetVelocity = Vec3.ZERO;

    public enum BulletType {
        CIRCLE(0.64f, 0.42f),
        SPARK(0.68f, 0.44f),
        STAR(0.84f, 0.58f),
        MENTOS(0.90f, 0.62f),
        BUBBLE(1.12f, 0.82f),
        BUTTERFLY(0.86f, 0.56f);

        private final float visualScale;
        private final float hitboxScale;

        BulletType(float visualScale, float hitboxScale) {
            this.visualScale = visualScale;
            this.hitboxScale = hitboxScale;
        }

        public float visualScale() {
            return visualScale;
        }

        public float hitboxScale() {
            return hitboxScale;
        }

        public static BulletType byId(int id) {
            BulletType[] values = values();
            return values[Math.floorMod(id, values.length)];
        }

        public String pathName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum BulletColor {
        RED(0.93f, 0.20f, 0.24f),
        BLUE(0.26f, 0.43f, 0.92f),
        GREEN(0.24f, 0.78f, 0.33f),
        LIME(0.62f, 0.92f, 0.18f),
        YELLOW(0.96f, 0.88f, 0.22f),
        WHITE(0.98f, 0.98f, 0.98f),
        PURPLE(0.63f, 0.28f, 0.92f),
        LIGHT_GRAY(0.82f, 0.82f, 0.84f),
        MAGENTA(0.92f, 0.18f, 0.82f),
        CYAN(0.18f, 0.88f, 0.95f);

        private final Vector3f dustColor;

        BulletColor(float r, float g, float b) {
            this.dustColor = new Vector3f(r, g, b);
        }

        public Vector3f dustColor() {
            return dustColor;
        }

        public static BulletColor byId(int id) {
            BulletColor[] values = values();
            return values[Math.floorMod(id, values.length)];
        }

        public String pathName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    @SuppressWarnings("unchecked")
    public TouhouPresetBulletProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(true);
        setDamageSpellId(ModSpells.DANMAKU_SHOT.getId().toString());
    }

    public TouhouPresetBulletProjectile(Level level, @Nullable Entity owner) {
        this(ModEntities.TOUHOU_PRESET_BULLET.get(), level);
        if (owner != null) {
            setOwner(owner);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BULLET_TYPE, BulletType.CIRCLE.ordinal());
        this.entityData.define(DATA_BULLET_COLOR, BulletColor.RED.ordinal());
    }

    @Override
    public int getMaxBounces() {
        return 0;
    }

    @Override
    public int getMaxLifetimeTicks() {
        return maxLifetimeTicks;
    }

    public void setMaxLifetimeTicksCustom(int ticks) {
        this.maxLifetimeTicks = Mth.clamp(ticks, 10, 600);
    }

    // ---- 多段階移動 API ----

    /**
     * 移動フェーズを追加する。弾をスポーンする前に呼ぶこと。
     */
    public void addMotionPhase(MotionPhase phase) {
        motionPhases.add(phase);
    }

    /**
     * 複数の移動フェーズを一括追加する。
     */
    public void addMotionPhases(List<MotionPhase> phases) {
        motionPhases.addAll(phases);
    }

    public boolean hasMotionPhases() {
        return !motionPhases.isEmpty();
    }

    @Override
    public void tick() {
        // 多段階移動がない場合は通常の挙動
        if (motionPhases.isEmpty()) {
            super.tick();
            return;
        }

        // 外部力の検出: SureHit/Guided が前回のtick後に deltaMovement を変更した分
        Vec3 externalForce = Vec3.ZERO;
        if (tickCount > 1) {
            Vec3 currentMotion = getDeltaMovement();
            externalForce = currentMotion.subtract(lastSetVelocity);
            // ごく小さい差は浮動小数点誤差なので無視
            if (externalForce.lengthSqr() < 1.0E-8D) {
                externalForce = Vec3.ZERO;
            }
        }

        if (phaseIndex < motionPhases.size()) {
            int age = Math.max(0, tickCount - 1);
            MotionPhase current = motionPhases.get(phaseIndex);
            int localTick = age - phaseStartAge;

            // フェーズの終了チェック
            while (localTick >= current.duration && phaseIndex < motionPhases.size() - 1) {
                phaseStartAge += current.duration;
                phaseIndex++;
                current = motionPhases.get(phaseIndex);
                localTick = age - phaseStartAge;
            }

            // 最終フェーズも終了した場合 → フェーズ制御を停止
            if (localTick >= current.duration && phaseIndex >= motionPhases.size() - 1) {
                Vec3 finalVel = current.getVelocity(current.duration - 1).add(externalForce);
                setDeltaMovement(finalVel);
                lastSetVelocity = finalVel;
                motionPhases.clear();
                super.tick();
                return;
            }

            // フェーズ速度を計算
            Vec3 phaseVel;
            Vec3 absPos = current.getAbsolutePosition(localTick);
            if (absPos != null) {
                // 絶対位置モード: 外部力があれば位置にオフセット追加
                Vec3 targetPos = absPos.add(externalForce);
                phaseVel = targetPos.subtract(position());
            } else {
                // 相対モード: フェーズ速度 + 外部力
                phaseVel = current.getVelocity(localTick).add(externalForce);
            }
            setDeltaMovement(phaseVel);
            lastSetVelocity = phaseVel;
        }

        super.tick();
    }

    public BulletType getBulletType() {
        return BulletType.byId(this.entityData.get(DATA_BULLET_TYPE));
    }

    public void setBulletType(BulletType bulletType) {
        this.entityData.set(DATA_BULLET_TYPE, bulletType.ordinal());
        refreshDimensions();
    }

    public BulletColor getBulletColor() {
        return BulletColor.byId(this.entityData.get(DATA_BULLET_COLOR));
    }

    public void setBulletColor(BulletColor bulletColor) {
        this.entityData.set(DATA_BULLET_COLOR, bulletColor.ordinal());
    }

    public float getVisualScale() {
        return getBulletType().visualScale();
    }

    public String getTexturePath() {
        return "textures/entity/touhou/bullet/" + getBulletType().pathName() + "/" + getBulletColor().pathName() + ".png";
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        float size = getBulletType().hitboxScale();
        return EntityDimensions.scalable(size, size);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_BULLET_TYPE.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();
        discard();
    }

    @Override
    protected void trailParticles() {
        var pos = getBoundingBox().getCenter();
        level().addParticle(new DustParticleOptions(getBulletColor().dustColor(), 1.0f), pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(getBulletColor().dustColor(), 1.1f), x, y, z, 8, 0.06D, 0.06D, 0.06D, 0.01D);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL,
                0.5f, 1.0f + level().getRandom().nextFloat() * 0.15f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.DANMAKU_SHOT.get() : spell;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BulletType", getBulletType().ordinal());
        tag.putInt("BulletColor", getBulletColor().ordinal());
        tag.putInt("MaxLifetimeTicks", maxLifetimeTicks);
        if (!motionPhases.isEmpty()) {
            tag.put("MotionPhases", MotionPhase.toListTag(motionPhases));
            tag.putInt("PhaseIndex", phaseIndex);
            tag.putInt("PhaseStartAge", phaseStartAge);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBulletType(BulletType.byId(tag.getInt("BulletType")));
        setBulletColor(BulletColor.byId(tag.getInt("BulletColor")));
        if (tag.contains("MaxLifetimeTicks")) {
            maxLifetimeTicks = tag.getInt("MaxLifetimeTicks");
        }
        if (tag.contains("MotionPhases")) {
            motionPhases.clear();
            motionPhases.addAll(MotionPhase.fromListTag(tag.getList("MotionPhases", Tag.TAG_COMPOUND)));
            phaseIndex = tag.getInt("PhaseIndex");
            phaseStartAge = tag.getInt("PhaseStartAge");
        }
        refreshDimensions();
    }
}
