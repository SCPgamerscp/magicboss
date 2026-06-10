package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ClownpieceSpell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import io.redspace.ironsspellbooks.damage.DamageSources;

import javax.annotation.Nullable;

/**
 * クラウンピース弾幕エンティティ。
 * bulletType:
 *   0 = mentos_blue  (Laser phase0)
 *   1 = mentos_red   (Laser phase1)
 *   2 = star_red     (Spread phase0)
 *   3 = spark_blue   (Spread phase1)
 */
public class ClownpieceBulletEntity extends Projectile implements IEntityAdditionalSpawnData {

    public int bulletType = 0;
    private float damage = 5f;
    private int life = 100;
    private int age = 0;

    // afterExpiry でレーザーを生成
    @Nullable public Vec3 afterExpiryDir = null;  // null = HomingTrail
    public int afterExpiryLaserType = 0;
    @Nullable public Vec3 targetPos = null;
    private float laserDamage = 5f;

    public ClownpieceBulletEntity(EntityType<? extends ClownpieceBulletEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ClownpieceBulletEntity(Level level, @Nullable LivingEntity owner,
                                   float damage, int life, int bulletType) {
        this(ModEntities.CLOWNPIECE_BULLET.get(), level);
        if (owner != null) setOwner(owner);
        this.damage     = damage;
        this.life       = life;
        this.bulletType = bulletType;
        this.addTag(ClownpieceSpell.CLOWNPIECE_ARROW_TAG);
    }

    @Override protected void defineSynchedData() {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.baseTick();
        if (++age > life) {
            if (!level().isClientSide) {
                if (afterExpiryDir != null) {
                    spawnLaser(afterExpiryDir, afterExpiryLaserType);
                } else if (targetPos != null) {
                    Vec3 d = targetPos.subtract(position());
                    if (d.lengthSqr() > 1e-4) spawnLaser(d.normalize(), afterExpiryLaserType);
                }
            }
            discard();
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) onHit(hit);
        setPos(position().add(getDeltaMovement()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (level().isClientSide) return;
        Entity target = result.getEntity();
        DamageSources.applyDamage(target, damage,
                io.redspace.ironsspellbooks.api.registry.SpellRegistry
                        .getSpell("elementmagicboss:clownpiece")
                        .getDamageSource(this, getOwner()));
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        discard();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        return super.canHitEntity(target)
                && target != owner
                && (owner == null || !owner.isAlliedTo(target));
    }

    private void spawnLaser(Vec3 dir, int type) {
        LivingEntity owner = null;
        Entity ownerE = getOwner();
        if (ownerE instanceof LivingEntity le) owner = le;
        ClownpieceLaserEntity laser = new ClownpieceLaserEntity(
                level(), owner, position(), dir, laserDamage, type);
        level().addFreshEntity(laser);
    }

    // afterExpiry セットアップ (SpreadTrail)
    public ClownpieceBulletEntity withSpreadTrail(Vec3 laserDir, int laserType, float lDamage) {
        this.afterExpiryDir       = laserDir;
        this.afterExpiryLaserType = laserType;
        this.laserDamage          = lDamage;
        return this;
    }

    // afterExpiry セットアップ (HomingTrail)
    public ClownpieceBulletEntity withHomingTrail(@Nullable Vec3 tPos, int laserType, float lDamage) {
        this.afterExpiryDir       = null;
        this.targetPos            = tPos;
        this.afterExpiryLaserType = laserType;
        this.laserDamage          = lDamage;
        return this;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        buf.writeFloat(damage);
        buf.writeInt(bulletType);
        Entity owner = getOwner();
        buf.writeInt(owner != null ? owner.getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        damage     = buf.readFloat();
        bulletType = buf.readInt();
        int id = buf.readInt();
        if (id >= 0) {
            Entity e = level().getEntity(id);
            if (e != null) setOwner(e);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage",     damage);
        tag.putInt("Life",         life);
        tag.putInt("Age",          age);
        tag.putInt("BulletType",   bulletType);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage     = tag.getFloat("Damage");
        life       = tag.getInt("Life");
        age        = tag.getInt("Age");
        bulletType = tag.getInt("BulletType");
    }
}
