package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.BlazingStarSpell;
import com.ecrea.elementmagicboss.spell.DanmakuShotSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlazingStarMeteorProjectile extends Comet {
    private float shardDamage;

    public BlazingStarMeteorProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public BlazingStarMeteorProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.BLAZING_STAR_METEOR.get(), level);
        setOwner(shooter);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        impactParticles(xOld, yOld, zOld);
        getImpactSound().ifPresent(this::doImpactSound);

        float explosionRadius = getExplosionRadius();
        Vec3 impactPos = hitResult.getLocation();
        for (Entity entity : level().getEntities(this, getBoundingBox().inflate(explosionRadius))) {
            if (canHitEntity(entity) && entity.distanceToSqr(impactPos) < explosionRadius * explosionRadius) {
                DamageSources.applyDamage(entity, getDamage(), ModSpells.BLAZING_STAR.get().getDamageSource(this, getOwner()));
            }
        }

        spawnBarrage(serverLevel, impactPos);
        discardHelper(hitResult);
    }

    private void spawnBarrage(ServerLevel level, Vec3 impactPos) {
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        if (owner == null) {
            return;
        }

        for (int i = 0; i < BlazingStarSpell.BARRAGE_COUNT; i++) {
            Vec3 direction = new Vec3(
                    random.nextDouble() * 2.0D - 1.0D,
                    random.nextDouble() * 0.75D + 0.15D,
                    random.nextDouble() * 2.0D - 1.0D
            ).normalize();
            DanmakuShotProjectile projectile = new DanmakuShotProjectile(level, owner);
            projectile.setPos(impactPos.add(direction.scale(0.25D)));
            projectile.setDamage(shardDamage);
            projectile.setDamageSpellId(ModSpells.BLAZING_STAR.getId().toString());
            projectile.setVariant(random.nextInt(DanmakuShotSpell.PELLET_COUNT));
            projectile.shoot(direction);
            level.addFreshEntity(projectile);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("ShardDamage", shardDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        shardDamage = tag.getFloat("ShardDamage");
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeFloat(shardDamage);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        super.readSpawnData(buffer);
        shardDamage = buffer.readFloat();
    }

    public void setShardDamage(float shardDamage) {
        this.shardDamage = shardDamage;
    }
}