package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

/**
 * 超大質量ブラックホール用ミサイル。
 * 着弾地点にHyperBlackHoleEntityをスポーンする。
 * BlackHoleSpellと同じ位置補正を適用。
 */
public class HyperBlackHoleMissile extends Projectile {

    private static final float BH_RADIUS = 30f;
    private float damage = 5.0f;
    private boolean spawned = false;

    public HyperBlackHoleMissile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public HyperBlackHoleMissile(Level level, LivingEntity owner, float damage) {
        this(ModEntities.HYPER_BLACK_HOLE_MISSILE.get(), level);
        this.setOwner(owner);
        this.damage = damage;
    }

    @Override protected void defineSynchedData() {}


    @Override
    public void tick() {
        super.tick();

        // トレイルパーティクル
        if (level().isClientSide) {
            Vec3 vec = getDeltaMovement();
            double length = vec.length();
            int count = (int) Math.min(20, Math.round(length) * 3) + 1;
            float f = (float) length / count;
            for (int i = 0; i < count; i++) {
                Vec3 p = vec.scale(f * i);
                level().addParticle(ParticleHelper.UNSTABLE_ENDER,
                        getX() + p.x, getY() + p.y, getZ() + p.z, 0, 0, 0);
            }
        }

        // 向き更新
        Vec3 motion = getDeltaMovement();
        double hSpeed = motion.horizontalDistance();
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        this.setXRot((float)(Mth.atan2(motion.y, hSpeed) * Mth.RAD_TO_DEG));

        // ヒット検出（移動前）
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit instanceof EntityHitResult entityHit) {
            hit = new EntityHitResult(entityHit.getEntity(),
                    entityHit.getEntity().getBoundingBox()
                            .clip(position(), position().add(getDeltaMovement()))
                            .orElse(position()));
        }
        if (hit.getType() != HitResult.Type.MISS && !spawned) {
            onHit(hit);
        }

        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner()
                && (getOwner() == null || !getOwner().isAlliedTo(target));
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (spawned) return;
        // BlackHoleSpell準拠: ブロック面の方向に応じてY補正
        Vec3 center = result.getLocation();
        Direction dir = result.getDirection();
        if (dir == Direction.DOWN) {
            center = center.subtract(0, BH_RADIUS * 2 - 1, 0); // 天井：BHを天井に押し付ける
        } else {
            // 地面(UP)・壁(horizontal)どちらも中心が着弾点になるよう下げる
            center = center.subtract(0, BH_RADIUS, 0);
        }
        spawnBlackHole(center);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (spawned) return;
        // エンティティの着弾点を基準に、コアがそこになるよう下げる
        Vec3 center = result.getLocation().subtract(0, BH_RADIUS, 0);
        spawnBlackHole(center);
    }

    private void spawnBlackHole(Vec3 center) {
        if (spawned || level().isClientSide) return;
        spawned = true;
        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;

        HyperBlackHoleEntity bh = new HyperBlackHoleEntity(level(), owner, damage);
        // BlackHoleSpell準拠: moveTo で中心位置を設定
        bh.moveTo(center);
        level().addFreshEntity(bh);

        level().playSound(null, center.x, center.y, center.z,
                SoundRegistry.BLACK_HOLE_CAST.get(), SoundSource.AMBIENT, 4f, 1f);
        MagicManager.spawnParticles(level(), ParticleHelper.UNSTABLE_ENDER,
                center.x, center.y, center.z, 50, 1, 1, 1, 0.5, true);
        discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) { damage = tag.getFloat("Damage"); }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) { tag.putFloat("Damage", damage); }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 16384.0; }
}
