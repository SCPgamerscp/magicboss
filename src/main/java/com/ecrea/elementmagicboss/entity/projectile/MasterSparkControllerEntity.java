package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.MasterSparkSpell;
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

public class MasterSparkControllerEntity extends Entity {
    private UUID ownerUUID;
    private float damage;
    private Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

    public MasterSparkControllerEntity(EntityType<? extends MasterSparkControllerEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public MasterSparkControllerEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.MASTER_SPARK_CONTROLLER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage = damage;
        this.forward = TouhouPatternHelper.safeNormalize(owner.getLookAngle(), new Vec3(0.0D, 0.0D, 1.0D));
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
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
            if (tickCount > 65) discard();
            return;
        }
        LivingEntity owner = getOwnerEntity();
        if (owner == null) {
            discard();
            return;
        }
        forward = TouhouPatternHelper.safeNormalize(owner.getLookAngle(), forward);
        setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        if (tickCount == 1) {
            spawnLaser(owner);
        }
        if (tickCount > 20 && tickCount <= 60) {
            spawnStars(owner);
            spawnSparks(owner);
        }
        if (tickCount > 62) {
            discard();
        }
    }

    private void spawnLaser(LivingEntity owner) {
        Vec3 start = owner.position().add(0.0D, owner.getEyeHeight() - 0.1D, 0.0D);
        TouhouLaserEntity laser = new TouhouLaserEntity(level(), owner, start, forward, damage, 80.0f, 20, 1,
                TouhouLaserEntity.LaserColor.YELLOW, MasterSparkSpell.SPELL_ID.toString());
        laser.addTag(TouhouLaserEntity.IFRAME_TAG);
        level().addFreshEntity(laser);
    }

    private void spawnStars(LivingEntity owner) {
        var random = level().getRandom();
        TouhouPatternHelper.Basis basis = TouhouPatternHelper.basis(forward);
        for (int i = 0; i < 8; i++) {
            double distance = 2.0D + i * 1.75D;
            Vec3 spawnPos = owner.position().add(0.0D, owner.getEyeHeight() - 0.1D, 0.0D).add(forward.scale(distance));
            Vec3 shot = TouhouPatternHelper.rotate(basis, random.nextDouble() * 30.0D - 15.0D, random.nextDouble() * 30.0D - 15.0D)
                    .scale(2.0D + random.nextDouble());
            TouhouPresetBulletProjectile projectile = new TouhouPresetBulletProjectile(level(), owner);
            projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            projectile.setDamage(damage);
            projectile.setDamageSpellId(MasterSparkSpell.SPELL_ID.toString());
            projectile.setBulletType(TouhouPresetBulletProjectile.BulletType.STAR);
            projectile.setBulletColor(TouhouPresetBulletProjectile.BulletColor.WHITE);
            projectile.setMaxLifetimeTicksCustom(40);
            projectile.setDeltaMovement(shot);
            projectile.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);
            level().addFreshEntity(projectile);
        }
    }

    private void spawnSparks(LivingEntity owner) {
        var random = level().getRandom();
        TouhouPatternHelper.Basis basis = TouhouPatternHelper.basis(forward);
        Vec3 origin = owner.position().add(0.0D, owner.getEyeHeight() - 0.1D, 0.0D);
        for (int i = 0; i < 4; i++) {
            Vec3 shot = TouhouPatternHelper.rotate(basis, random.nextDouble() * 90.0D - 45.0D, random.nextDouble() * 90.0D - 45.0D)
                    .scale(0.8D + random.nextDouble() * 0.3D);
            TouhouPresetBulletProjectile projectile = new TouhouPresetBulletProjectile(level(), owner);
            projectile.setPos(origin.x + shot.x * 1.3D, origin.y + shot.y * 1.3D, origin.z + shot.z * 1.3D);
            projectile.setDamage(damage);
            projectile.setDamageSpellId(MasterSparkSpell.SPELL_ID.toString());
            projectile.setBulletType(TouhouPresetBulletProjectile.BulletType.SPARK);
            projectile.setBulletColor(TouhouPresetBulletProjectile.BulletColor.YELLOW);
            projectile.setMaxLifetimeTicksCustom(36);
            projectile.setDeltaMovement(shot);
            projectile.addTag(TouhouPresetBulletProjectile.IFRAME_TAG);
            level().addFreshEntity(projectile);
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
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putFloat("Damage", damage);
        tag.putDouble("ForwardX", forward.x);
        tag.putDouble("ForwardY", forward.y);
        tag.putDouble("ForwardZ", forward.z);
    }
}
