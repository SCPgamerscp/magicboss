package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.PoisonSplashStormSpell;
import io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonSplash;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class PoisonSplashStormFieldEntity extends Entity {
    private static final int LIFETIME = 200;
    private static final int WAVE_INTERVAL = 5;
    private static final float RADIUS = 20.0f;

    private UUID ownerUUID;
    private float splashDamage;
    private int effectDuration;

    public PoisonSplashStormFieldEntity(EntityType<? extends PoisonSplashStormFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public PoisonSplashStormFieldEntity(Level level, LivingEntity owner, float splashDamage, int effectDuration) {
        this(ModEntities.POISON_SPLASH_STORM_FIELD.get(), level);
        this.ownerUUID = owner.getUUID();
        this.splashDamage = splashDamage;
        this.effectDuration = effectDuration;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount > LIFETIME) {
            discard();
            return;
        }
        if (!level().isClientSide && tickCount % WAVE_INTERVAL == 1) {
            spawnWave();
        }
    }

    private void spawnWave() {
        RandomSource random = level().getRandom();
        int count = 6 + random.nextInt(5);
        LivingEntity owner = getOwnerEntity();
        for (int i = 0; i < count; i++) {
            double distance = RADIUS * random.nextDouble() * random.nextDouble();
            double angle = random.nextDouble() * Math.PI * 2.0D;
            Vec3 pos = position().add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
            PoisonSplash splash = new PoisonSplash(level());
            if (owner != null) {
                splash.setOwner(owner);
            }
            splash.moveTo(pos.x, pos.y, pos.z, random.nextFloat() * 360.0F, 0.0F);
            splash.setDamage(splashDamage);
            splash.setEffectDuration(effectDuration);
            splash.addTag(PoisonSplashStormSpell.POISON_SPLASH_STORM_TAG);
            level().addFreshEntity(splash);
        }
    }

    @Nullable
    private LivingEntity getOwnerEntity() {
        if (ownerUUID == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        splashDamage = tag.getFloat("SplashDamage");
        effectDuration = tag.getInt("EffectDuration");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        tag.putFloat("SplashDamage", splashDamage);
        tag.putInt("EffectDuration", effectDuration);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }
}
