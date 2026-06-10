package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

/**
 * トライデントボム - 爆散後の個々のトライデント弾
 * Projectile継承でProjectileUtil.getHitResultOnMoveVectorを使用。
 * EndCrystalを含む全isPickable()エンティティにヒット可能。
 * 無敵時間を無視してダメージを与える。
 */
public class TridentShardEntity extends Projectile {
    public static final String IFRAME_TAG = "elementmagicboss_trident_shard_iframe";

    private float damage   = 5.0f;
    private int   lifetime = 80;
    private int   bounces  = 0;
    private static final int MAX_BOUNCES = 30;

    public TridentShardEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.addTag(IFRAME_TAG);
    }

    public TridentShardEntity(Level level, LivingEntity owner, float damage, Vec3 velocity) {
        this(ModEntities.TRIDENT_SHARD.get(), level);
        this.setOwner(owner);
        this.damage = damage;
        this.setDeltaMovement(velocity);
        this.addTag(IFRAME_TAG);
        // 初期向きを速度から計算
        double hSpeed = velocity.horizontalDistance();
        this.setYRot((float)(Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG));
        this.setXRot((float)(Mth.atan2(velocity.y, hSpeed)     * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }


    @Override
    public void tick() {
        super.tick();

        // 向きを速度に合わせて更新（AbstractMagicProjectile#rotateWithMotion相当）
        Vec3 motion = getDeltaMovement();
        double hSpeed = motion.horizontalDistance();
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        this.setXRot((float)(Mth.atan2(motion.y, hSpeed)   * Mth.RAD_TO_DEG));

        // 移動・重力
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        setDeltaMovement(motion.x * 0.99, motion.y - 0.04, motion.z * 0.99);

        lifetime--;
        if (lifetime <= 0) { discard(); return; }

        // ProjectileUtilによるヒット検出
        // canHitEntity()が isPickable() を通じてEndCrystalも含む全エンティティを検出する
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return super.canHitEntity(target) && target != getOwner()
                && (getOwner() == null || !getOwner().isAlliedTo(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        Entity owner = getOwner();

        resetInvulnerable(target);

        DamageSources.applyDamage(target, damage,
                ModSpells.TRIDENT_BOMB.get().getDamageSource(this,
                        owner instanceof LivingEntity le ? le : null).setIFrames(0));

        resetInvulnerable(target);
    }

    /** hurt()の前後に呼んでinvulnerableTimeを確実に0にする */
    private static void resetInvulnerable(Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        } else if (entity instanceof EnderDragonPart part) {
            part.parentMob.invulnerableTime = 0;
        } else if (entity instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity livingParent) {
            livingParent.invulnerableTime = 0;
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (bounces >= MAX_BOUNCES) {
            discard();
            return;
        }
        bounces++;
        // 当たった面の法線方向で速度を反射
        Direction face = result.getDirection();
        Vec3 motion = getDeltaMovement();
        Vec3 reflected = switch (face.getAxis()) {
            case X -> new Vec3(-motion.x, motion.y, motion.z);
            case Y -> new Vec3(motion.x, -motion.y, motion.z);
            case Z -> new Vec3(motion.x, motion.y, -motion.z);
        };
        // 少し減衰させる
        setDeltaMovement(reflected.scale(0.85));
        // めり込み防止：面の法線方向に少しずらす
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        setPos(getX() + normal.x * 0.1, getY() + normal.y * 0.1, getZ() + normal.z * 0.1);
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage   = tag.getFloat("Damage");
        lifetime = tag.getInt("Lifetime");
        bounces  = tag.getInt("Bounces");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage",   damage);
        tag.putInt("Lifetime",   lifetime);
        tag.putInt("Bounces",    bounces);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 16384.0; }
}
