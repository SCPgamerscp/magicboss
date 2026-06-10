package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;

public class CrimsonYoungMoonProjectile extends DanmakuShotProjectile {
    private static final int MAX_LIFETIME = 60;
    private static final Vector3f CRIMSON = new Vector3f(0.95f, 0.12f, 0.22f);

    @SuppressWarnings("unchecked")
    public CrimsonYoungMoonProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(true);
    }

    public CrimsonYoungMoonProjectile(Level level, Entity owner) {
        this(ModEntities.CRIMSON_YOUNG_MOON_PROJECTILE.get(), level);
        setOwner(owner);
        setDamageSpellId(ModSpells.CRIMSON_YOUNG_MOON.getId().toString());
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
    protected void onHitBlock(BlockHitResult result) {
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();
        discard();
    }

    @Override
    protected void trailParticles() {
        var pos = getBoundingBox().getCenter();
        level().addParticle(new DustParticleOptions(CRIMSON, 1.0f), pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
        level().addParticle(ParticleTypes.CRIMSON_SPORE, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new DustParticleOptions(CRIMSON, 1.25f), x, y, z, 10, 0.08, 0.08, 0.08, 0.01);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, 8, 0.12, 0.12, 0.12, 0.01);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL,
                0.45f, 0.6f + level().getRandom().nextFloat() * 0.2f);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.CRIMSON_YOUNG_MOON.get() : spell;
    }
}
