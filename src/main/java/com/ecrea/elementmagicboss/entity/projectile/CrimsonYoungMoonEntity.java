package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.CrimsonYoungMoonSpell;
import com.ecrea.elementmagicboss.spell.DanmakuShotSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class CrimsonYoungMoonEntity extends Entity {
    private static final float PROJECTILE_SPEED = 0.95f;

    private UUID ownerUUID;
    private float projectileDamage;

    public CrimsonYoungMoonEntity(EntityType<? extends CrimsonYoungMoonEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public CrimsonYoungMoonEntity(Level level, LivingEntity owner, float projectileDamage) {
        this(ModEntities.CRIMSON_YOUNG_MOON.get(), level);
        this.ownerUUID = owner.getUUID();
        this.projectileDamage = projectileDamage;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnAmbientClientParticles();
            return;
        }

        if (tickCount >= CrimsonYoungMoonSpell.DURATION_TICKS) {
            discard();
            return;
        }

        if (tickCount % CrimsonYoungMoonSpell.BURST_INTERVAL_TICKS == 0) {
            spawnBurst();
        }
    }

    private void spawnBurst() {
        LivingEntity owner = getOwnerEntity();
        Vec3 origin = position();
        RandomSource random = level().getRandom();
        for (int i = 0; i < CrimsonYoungMoonSpell.PROJECTILES_PER_BURST; i++) {
            Vec3 direction = randomDirection(random);

            CrimsonYoungMoonProjectile projectile = new CrimsonYoungMoonProjectile(level(), owner != null ? owner : this);
            projectile.setPos(origin.x, origin.y, origin.z);
            projectile.setDamage(projectileDamage);
            projectile.setVariant(random.nextInt(DanmakuShotSpell.PELLET_COUNT));
            projectile.setDamageSpellId(ModSpells.CRIMSON_YOUNG_MOON.getId().toString());
            projectile.setDeltaMovement(direction.scale(PROJECTILE_SPEED));
            level().addFreshEntity(projectile);
        }
    }

    private static Vec3 randomDirection(RandomSource random) {
        float yaw = random.nextFloat() * 360.0f;
        float pitch = -90.0f + random.nextFloat() * 180.0f;
        return Vec3.directionFromRotation(pitch, yaw).normalize();
    }

    private void spawnAmbientClientParticles() {
        for (int i = 0; i < 4; i++) {
            double angle = (tickCount * 0.04D) + (Math.PI * 2.0D * i / 4.0D);
            double radius = 3.2D + level().getRandom().nextDouble() * 0.8D;
            double x = getX() + Math.cos(angle) * radius;
            double y = getY() + (level().getRandom().nextDouble() - 0.5D) * 1.2D;
            double z = getZ() + Math.sin(angle) * radius;
            level().addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0.0, 0.002, 0.0);
            level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.001, 0.0);
        }
    }

    public float getPulseScale(float partialTick) {
        float age = tickCount + partialTick;
        return 1.0f + Mth.sin(age * 0.08f) * 0.06f;
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        projectileDamage = tag.getFloat("ProjectileDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("ProjectileDamage", projectileDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 2048.0D * 2048.0D;
    }
}
