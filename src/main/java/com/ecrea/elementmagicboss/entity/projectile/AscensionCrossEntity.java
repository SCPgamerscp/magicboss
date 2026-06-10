package com.ecrea.elementmagicboss.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class AscensionCrossEntity extends Entity {
    public static final int LIFETIME_TICKS = 60;

    public AscensionCrossEntity(EntityType<? extends AscensionCrossEntity> type, Level level) {
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
        setPos(getX(), getY() + 0.08, getZ());

        if (level().isClientSide) {
            spawnParticles();
        }

        if (tickCount >= LIFETIME_TICKS) {
            discard();
        }
    }

    private void spawnParticles() {
        RandomSource random = level().getRandom();
        for (int i = 0; i < 2; i++) {
            double x = getX() + (random.nextDouble() - 0.5) * 0.8;
            double y = getY() + random.nextDouble() * 2.5;
            double z = getZ() + (random.nextDouble() - 0.5) * 0.8;
            level().addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.01, 0.0);
            level().addParticle(ParticleTypes.END_ROD, x, y, z, 0.0, 0.02, 0.0);
        }
    }

    public float getAlpha(float partialTick) {
        return Math.max(0.0f, 1.0f - ((tickCount + partialTick) / (float) LIFETIME_TICKS));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
