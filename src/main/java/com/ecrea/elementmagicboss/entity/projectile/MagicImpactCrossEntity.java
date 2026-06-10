package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.spell.MagicImpactSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class MagicImpactCrossEntity extends Entity {
    public static final float DAMAGE_RADIUS = 100.0f;
    public static final float CROSS_HEIGHT = 56.0f;
    public static final float CROSS_WIDTH = 28.0f;

    private UUID ownerUUID;
    private float spellDamage;

    public MagicImpactCrossEntity(EntityType<? extends MagicImpactCrossEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
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

        if (tickCount >= MagicImpactSpell.DURATION_TICKS) {
            discard();
            return;
        }

        if (tickCount % MagicImpactSpell.DAMAGE_INTERVAL_TICKS == 0) {
            applyAreaDamage();
        }

        if (tickCount % 10 == 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    getX(), getY() + CROSS_HEIGHT * 0.6, getZ(),
                    24, 2.5, CROSS_HEIGHT * 0.4, 2.5, 0.02);
            serverLevel.sendParticles(ParticleTypes.GLOW,
                    getX(), getY() + 3.0, getZ(),
                    16, 1.2, 1.2, 1.2, 0.01);
        }
    }

    private void applyAreaDamage() {
        LivingEntity owner = getOwnerEntity();
        AABB area = AABB.ofSize(position(), DAMAGE_RADIUS * 2.0, DAMAGE_RADIUS * 2.0, DAMAGE_RADIUS * 2.0);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, living ->
                living.isAlive()
                        && living != owner
                        && living.distanceToSqr(position()) <= DAMAGE_RADIUS * DAMAGE_RADIUS
                        && !isFriendly(owner, living));

        Entity attacker = owner != null ? owner : this;
        for (LivingEntity target : targets) {
            target.invulnerableTime = 0;
            DamageSources.applyDamage(
                    target,
                    spellDamage,
                    ModSpells.MAGIC_IMPACT.get().getDamageSource(this, attacker)
            );
            target.invulnerableTime = 0;
        }
    }

    private boolean isFriendly(@Nullable LivingEntity owner, LivingEntity target) {
        return owner != null && DamageSources.isFriendlyFireBetween(owner, target);
    }

    private void spawnAmbientClientParticles() {
        RandomSource random = level().getRandom();
        for (int i = 0; i < 6; i++) {
            double y = getY() + random.nextDouble() * CROSS_HEIGHT;
            double x = getX() + (random.nextDouble() - 0.5) * 6.0;
            double z = getZ() + (random.nextDouble() - 0.5) * 6.0;
            level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    public void setSpellDamage(float spellDamage) {
        this.spellDamage = spellDamage;
    }

    public float getAgeScale(float partialTick) {
        float age = tickCount + partialTick;
        if (age < 20.0f) {
            return age / 20.0f;
        }
        return 1.0f;
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    public void setOwner(LivingEntity owner) {
        this.ownerUUID = owner.getUUID();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        spellDamage = tag.getFloat("SpellDamage");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("SpellDamage", spellDamage);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 1024.0D * 1024.0D;
    }
}
