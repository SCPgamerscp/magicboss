package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
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

import java.util.Random;

/**
 * トライデントボム - メインの投擲弾
 * 着弾（ブロック・エンティティ）時に500本のトライデント弾を爆散させる
 */
public class TridentBombProjectile extends Projectile {
    public static final String IFRAME_TAG = "elementmagicboss_trident_bomb_iframe";

    private float damage    = 10.0f;
    private boolean exploded = false;


    public TridentBombProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.addTag(IFRAME_TAG);
    }

    public TridentBombProjectile(Level level, LivingEntity owner, float damage) {
        this(ModEntities.TRIDENT_BOMB.get(), level);
        this.damage = damage;
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        this.addTag(IFRAME_TAG);
    }

    /** 発射方向を設定（look vectorから呼ぶ）*/
    public void shootFromLook(LivingEntity shooter, float speed) {
        Vec3 look = shooter.getLookAngle();
        this.shoot(look.x, look.y, look.z, speed, 0.0f);
    }

    @Override
    public void tick() {
        super.tick();

        // 着弾チェック (ProjectileUtil) - canHitEntity()に判定を委ねる
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        // AbstractMagicProjectile準拠: エンティティヒット位置をBoundingBox表面の正確な座標に補正
        if (hit instanceof EntityHitResult entityHit) {
            Vec3 correctedLoc = entityHit.getEntity().getBoundingBox()
                    .clip(this.position(), this.position().add(this.getDeltaMovement()))
                    .orElse(this.position());
            hit = new EntityHitResult(entityHit.getEntity(), correctedLoc);
        }

        if (hit.getType() != HitResult.Type.MISS && !exploded) {
            onHit(hit);
        }

        Vec3 motion = getDeltaMovement();
        // 移動
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        // 重力
        if (!isNoGravity()) {
            setDeltaMovement(motion.x * 0.99, motion.y - 0.05, motion.z * 0.99);
        }
        // 向き更新
        double hSpeed = motion.horizontalDistance();
        this.setYRot((float)(Mth.atan2(motion.x, motion.z) * (180.0 / Math.PI)));
        this.setXRot((float)(Mth.atan2(motion.y, hSpeed)   * (180.0 / Math.PI)));
    }


    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!exploded) spawnShards(position());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        // 爆散位置は実際の衝突点
        Vec3 spawnPos = result.getLocation();
        Entity owner = getOwner();
        LivingEntity livingOwner = owner instanceof LivingEntity living ? living : null;

        resetInvulnerable(target);
        DamageSources.applyDamage(target, damage * 2.0f,
                ModSpells.TRIDENT_BOMB.get().getDamageSource(this, livingOwner).setIFrames(0));
        resetInvulnerable(target);

        if (!exploded) spawnShards(spawnPos);
    }

    private static void resetInvulnerable(Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        } else if (entity instanceof EnderDragonPart part) {
            part.parentMob.invulnerableTime = 0;
        } else if (entity instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity livingParent) {
            livingParent.invulnerableTime = 0;
        }
    }

    /** 着弾地点から500本のトライデントを全方向に爆散 */
    private void spawnShards(Vec3 pos) {
        exploded = true;
        discard();
        if (level().isClientSide) return;

        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
        Random rand = new Random();

        for (int i = 0; i < 2000; i++) {
            // 球面上のランダム方向
            double theta = rand.nextDouble() * Math.PI * 2;
            double phi   = Math.acos(2 * rand.nextDouble() - 1);
            double speed = 0.8 + rand.nextDouble() * 2.0;
            double dx = speed * Math.sin(phi) * Math.cos(theta);
            double dy = speed * Math.cos(phi);
            double dz = speed * Math.sin(phi) * Math.sin(theta);

            TridentShardEntity shard = new TridentShardEntity(level(),
                    owner, damage, new Vec3(dx, dy, dz));
            shard.setPos(pos.x, pos.y, pos.z);
            level().addFreshEntity(shard);
        }
    }


    @Override protected void defineSynchedData() {}

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

    // TridentShardEntity のコンストラクタ用 owner=null 対応ヘルパー
    // (owner が LivingEntity 以外のとき spawnShards で null になる場合のダミー)
    private static class DummyOwnerFallback {}
}
