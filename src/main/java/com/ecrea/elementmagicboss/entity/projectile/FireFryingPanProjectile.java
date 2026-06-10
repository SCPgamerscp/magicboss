package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class FireFryingPanProjectile extends DanmakuShotProjectile {
    public static final float GRAVITY_PER_TICK = 0.035f;
    private static final int MAX_LIFETIME = 90;

    @SuppressWarnings("unchecked")
    public FireFryingPanProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(false);
    }

    public FireFryingPanProjectile(Level level, Entity owner) {
        this(ModEntities.FIRE_FRYING_PAN_PROJECTILE.get(), level);
        setOwner(owner);
        setDamageSpellId(ModSpells.FIRE_FRYING_PAN.getId().toString());
    }

    @Override
    public int getMaxBounces() {
        return 0;
    }

    @Override
    public int getMaxLifetimeTicks() {
        return MAX_LIFETIME;
    }

    @Override
    public float getSpeed() {
        return 1.0f;
    }

    @Override
    public void tick() {
        setDeltaMovement(getDeltaMovement().add(0, -GRAVITY_PER_TICK, 0));
        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();
        discard();
    }

    @Override
    protected void trailParticles() {
        Level level = level();
        Vec3 pos = getBoundingBox().getCenter();
        level.addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0, 0.01, 0.0);
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 6, 0.08, 0.08, 0.08, 0.03);
            serverLevel.sendParticles(ParticleTypes.LAVA, x, y, z, 2, 0.02, 0.02, 0.02, 0.01);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.NEUTRAL,
                0.4f, 1.4f + level().getRandom().nextFloat() * 0.3f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.FIRE_FRYING_PAN.get() : spell;
    }
}
