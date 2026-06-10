package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * アビスブラスト（深淵砲）のビームエンティティ。
 * Cataclysm の Abyss_Blast_Entity をプレイヤー詠唱用に移植。
 * 詠唱者の視線方向へ追随するビームを照射し、貫通先の敵にダメージを与える。
 */
public class AbyssBlastEntity extends Entity {
    public static final double RADIUS = 50;

    public LivingEntity caster;
    public double endPosX, endPosY, endPosZ;
    public double collidePosX, collidePosY, collidePosZ;
    public double prevCollidePosX, prevCollidePosY, prevCollidePosZ;
    public float renderYaw, renderPitch;

    /** appear アニメーション（0〜maxTimer で膨張/縮退） */
    private int appearTimer = 0;
    private static final int APPEAR_MAX = 3;

    public boolean on = true;
    public net.minecraft.core.Direction blockSide = null;

    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(AbyssBlastEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(AbyssBlastEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(AbyssBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(AbyssBlastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(AbyssBlastEntity.class, EntityDataSerializers.FLOAT);

    public float prevYaw;
    public float prevPitch;

    public AbyssBlastEntity(EntityType<? extends AbyssBlastEntity> type, Level level) {
        super(type, level);
        noCulling = true;
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * プレイヤー詠唱用コンストラクタ。
     */
    public AbyssBlastEntity(EntityType<? extends AbyssBlastEntity> type, Level level,
                            LivingEntity caster, double x, double y, double z,
                            float yaw, float pitch, int duration, float damage) {
        this(type, level);
        this.caster = caster;
        this.setBeamYaw(yaw);
        this.setBeamPitch(pitch);
        this.setDuration(duration);
        this.setDamage(damage);
        this.setPos(x, y, z);
        this.calculateEndPos();
        if (!level.isClientSide) {
            this.setCasterID(caster.getId());
        }
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public void tick() {
        super.tick();
        prevCollidePosX = collidePosX;
        prevCollidePosY = collidePosY;
        prevCollidePosZ = collidePosZ;
        prevYaw = renderYaw;
        prevPitch = renderPitch;
        xo = getX();
        yo = getY();
        zo = getZ();

        if (tickCount == 1 && level().isClientSide) {
            caster = (LivingEntity) level().getEntity(getCasterID());
        }

        // プレイヤーの視線方向に追随
        if (!level().isClientSide && caster != null && caster.isAlive()) {
            float newYaw = (float) ((caster.yHeadRot + 90) * Math.PI / 180.0d);
            float newPitch = (float) (-caster.getXRot() * Math.PI / 180.0d);
            this.setBeamYaw(newYaw);
            this.setBeamPitch(newPitch);
            this.setPos(caster.getX(), caster.getEyeY(), caster.getZ());
        }

        if (caster != null) {
            renderYaw = (float) ((caster.yHeadRot + 90) * Math.PI / 180.0d);
            renderPitch = (float) (-caster.getXRot() * Math.PI / 180.0d);
        }

        // 初回tickで prev 値を現在値に合わせる（補間による横向き描画を防止）
        if (tickCount <= 2) {
            prevYaw = renderYaw;
            prevPitch = renderPitch;
        }

        // appear animation (即座にビーム展開)
        if (!on && appearTimer == 0) {
            this.discard();
        }
        if (on) {
            if (appearTimer < APPEAR_MAX) appearTimer++;
        } else {
            if (appearTimer > 0) appearTimer--;
        }

        if (caster != null && !caster.isAlive()) discard();

        // 即座にダメージ開始（チャージはスペル側のLONG castで完了済み）
        this.calculateEndPos();
        LaserHitResult hit = raytraceEntities(level(),
                new Vec3(getX(), getY(), getZ()),
                new Vec3(endPosX, endPosY, endPosZ));

        if (!level().isClientSide) {
            for (LivingEntity target : hit.entities) {
                if (caster != null && !DamageSources.isFriendlyFireBetween(caster, target) && target != caster) {
                    target.invulnerableTime = 0;
                    DamageSources.applyDamage(
                            target,
                            getDamage(),
                            ModSpells.ABYSS_BLAST.get().getDamageSource(this, caster)
                    );
                    target.invulnerableTime = 0;
                }
            }
        }

        if (tickCount > getDuration()) {
            on = false;
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(YAW, 0F);
        this.entityData.define(PITCH, 0F);
        this.entityData.define(DURATION, 0);
        this.entityData.define(CASTER_ID, -1);
        this.entityData.define(DAMAGE, 0F);
    }

    // ── getter/setter ──
    public float getBeamYaw() { return entityData.get(YAW); }
    public void setBeamYaw(float yaw) { entityData.set(YAW, yaw); }
    public float getBeamPitch() { return entityData.get(PITCH); }
    public void setBeamPitch(float pitch) { entityData.set(PITCH, pitch); }
    public int getDuration() { return entityData.get(DURATION); }
    public void setDuration(int d) { entityData.set(DURATION, d); }
    public int getCasterID() { return entityData.get(CASTER_ID); }
    public void setCasterID(int id) { entityData.set(CASTER_ID, id); }
    public float getDamage() { return entityData.get(DAMAGE); }
    public void setDamage(float d) { entityData.set(DAMAGE, d); }

    public int getAppearTimer() { return appearTimer; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.setBeamYaw(tag.getFloat("Yaw"));
        this.setBeamPitch(tag.getFloat("Pitch"));
        this.setDuration(tag.getInt("Duration"));
        this.setDamage(tag.getFloat("Damage"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Yaw", this.getBeamYaw());
        tag.putFloat("Pitch", this.getBeamPitch());
        tag.putInt("Duration", this.getDuration());
        tag.putFloat("Damage", this.getDamage());
    }

    private void calculateEndPos() {
        if (level().isClientSide()) {
            endPosX = getX() + RADIUS * Math.cos(renderYaw) * Math.cos(renderPitch);
            endPosZ = getZ() + RADIUS * Math.sin(renderYaw) * Math.cos(renderPitch);
            endPosY = getY() + RADIUS * Math.sin(renderPitch);
        } else {
            endPosX = getX() + RADIUS * Math.cos(getBeamYaw()) * Math.cos(getBeamPitch());
            endPosZ = getZ() + RADIUS * Math.sin(getBeamYaw()) * Math.cos(getBeamPitch());
            endPosY = getY() + RADIUS * Math.sin(getBeamPitch());
        }
    }

    public LaserHitResult raytraceEntities(Level world, Vec3 from, Vec3 to) {
        LaserHitResult result = new LaserHitResult();
        result.setBlockHit(world.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)));
        if (result.blockHit != null) {
            Vec3 hitVec = result.blockHit.getLocation();
            collidePosX = hitVec.x;
            collidePosY = hitVec.y;
            collidePosZ = hitVec.z;
            blockSide = result.blockHit.getDirection();
        } else {
            collidePosX = endPosX;
            collidePosY = endPosY;
            collidePosZ = endPosZ;
            blockSide = null;
        }
        List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class,
                new AABB(Math.min(getX(), collidePosX), Math.min(getY(), collidePosY), Math.min(getZ(), collidePosZ),
                        Math.max(getX(), collidePosX), Math.max(getY(), collidePosY), Math.max(getZ(), collidePosZ)).inflate(2, 2, 2));
        for (LivingEntity entity : entities) {
            if (entity == caster) continue;
            float pad = entity.getPickRadius() + 0.5f;
            AABB aabb = entity.getBoundingBox().inflate(pad, pad, pad);
            Optional<Vec3> hit = aabb.clip(from, to);
            if (aabb.contains(from) || hit.isPresent()) {
                result.addEntityHit(entity);
            }
        }
        return result;
    }

    @Override public void push(Entity e) {}
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }

    @Override
    public boolean shouldRenderAtSqrDistance(double dist) {
        return dist < 1024.0D * 1024.0D;
    }

    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static class LaserHitResult {
        public BlockHitResult blockHit;
        public final List<LivingEntity> entities = new ArrayList<>();

        public void setBlockHit(HitResult ray) {
            if (ray.getType() == HitResult.Type.BLOCK)
                this.blockHit = (BlockHitResult) ray;
        }

        public void addEntityHit(LivingEntity entity) {
            entities.add(entity);
        }
    }
}
