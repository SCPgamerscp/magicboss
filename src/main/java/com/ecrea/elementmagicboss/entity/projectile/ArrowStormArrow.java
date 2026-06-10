package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;
import java.util.List;

/**
 * MagicArrowProjectileと完全に同じ動作。無敵時間無視。
 */
public class ArrowStormArrow extends Projectile {

    private float damage = 5.0f;
    private final List<Entity> victims = new ArrayList<>();
    private int hitsPerTick = 0;

    public ArrowStormArrow(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ArrowStormArrow(Level level, LivingEntity owner, float damage) {
        this(ModEntities.ARROW_STORM_ARROW.get(), level);
        this.setOwner(owner);
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {}


    @Override
    public void tick() {
        super.tick();
        hitsPerTick = 0;

        // トレイルパーティクル（クライアントのみ）
        if (level().isClientSide) {
            Vec3 vec = getDeltaMovement();
            double length = vec.length();
            int count = (int) Math.min(20, Math.round(length) * 2) + 1;
            float f = (float) length / count;
            for (int i = 0; i < count; i++) {
                Vec3 p = vec.scale(f * i);
                level().addParticle(ParticleHelper.UNSTABLE_ENDER,
                        getX() + p.x, getY() + p.y, getZ() + p.z, 0, 0, 0);
            }
        }

        // ── AbstractMagicProjectile準拠: ヒット検出を移動より先に行う ──
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        // エンティティヒット位置をBoundingBox表面の正確な座標に補正
        if (hitresult instanceof EntityHitResult entityHit) {
            hitresult = new EntityHitResult(entityHit.getEntity(),
                    entityHit.getEntity().getBoundingBox()
                            .clip(this.position(), this.position().add(this.getDeltaMovement()))
                            .orElse(this.position()));
        }
        if (hitresult.getType() != HitResult.Type.MISS) {
            onHit(hitresult);
        }

        // ── 移動 ──
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);

        // 向き更新
        double hSpeed = motion.horizontalDistance();
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        this.setXRot((float)(Mth.atan2(motion.y, hSpeed) * Mth.RAD_TO_DEG));
    }


    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner()
                && (getOwner() == null || !getOwner().isAlliedTo(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // MagicArrowProjectile#onHitEntity と完全に同じ構造
        Entity target = result.getEntity();
        if (!victims.contains(target)) {
            if (target instanceof LivingEntity living) {
                living.invulnerableTime = 0;
            }
            DamageSources.applyDamage(target, damage,
                    ModSpells.ARROW_STORM.get().getDamageSource(this,
                            getOwner() instanceof LivingEntity le ? le : null).setIFrames(0));
            victims.add(target);
        }
        // 無限貫通 - 1tickあたり最大5回まで連続ヒット（MagicArrowProjectile準拠）
        if (hitsPerTick++ < 5) {
            HitResult next = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (next.getType() != HitResult.Type.MISS) {
                onHit(next);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        // ブロックは貫通（MagicArrowProjectile準拠）
    }

    @Override
    protected void onHit(HitResult result) {
        if (!level().isClientSide && result.getType() == HitResult.Type.ENTITY) {
            level().playSound(null, BlockPos.containing(position()),
                    SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2f, 0.65f);
        }
        super.onHit(result);
    }

    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level(), ParticleHelper.UNSTABLE_ENDER,
                x, y, z, 10, .1, .1, .1, .4, false);
    }

    public void setDamage(float damage) { this.damage = damage; }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getFloat("Damage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 16384.0; }
}
