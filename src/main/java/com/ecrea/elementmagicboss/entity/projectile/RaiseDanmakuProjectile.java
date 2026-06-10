package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RaiseDanmakuProjectile extends DanmakuShotProjectile {

    private static final float GRAVITY = 0.05f;
    private static final int RAISE_MAX_BOUNCES  = 10;
    // 寿命を延ばす: 500tick → 1200tick (60秒)
    private static final int RAISE_MAX_LIFETIME = 1200;

    @SuppressWarnings("unchecked")
    public RaiseDanmakuProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super((EntityType<? extends DanmakuShotProjectile>) entityType, level);
        this.setNoGravity(false);
    }

    public RaiseDanmakuProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.RAISE_DANMAKU_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamageSpellId(ModSpells.RAISE_DANMAKU.getId().toString());
    }

    @Override
    public int getMaxBounces() {
        return RAISE_MAX_BOUNCES;
    }

    @Override
    public int getMaxLifetimeTicks() {
        return RAISE_MAX_LIFETIME;
    }

    /** 重力を毎tick追加 */
    @Override
    public void tick() {
        setDeltaMovement(getDeltaMovement().add(0, -GRAVITY, 0));
        super.tick();
    }

    @Override
    protected void trailParticles() {
        Level lv = level();
        if (lv.isClientSide) {
            Vec3 pos = getBoundingBox().getCenter();
            lv.addParticle(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
            lv.addParticle(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 0.0, 0.02, 0.0);
        }
    }

    @Override
    protected void impactParticles(double x, double y, double z) {
        Level lv = level();
        if (lv instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 8, 0.1, 0.1, 0.1, 0.05);
            serverLevel.sendParticles(ParticleTypes.LAVA,  x, y, z, 3, 0.1, 0.1, 0.1, 0.01);
        }
    }

    @Override
    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.FIRE_AMBIENT, SoundSource.NEUTRAL,
                0.6F, 1.2F + level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(getDamageSpellId());
        return spell == SpellRegistry.none() ? ModSpells.RAISE_DANMAKU.get() : spell;
    }
}
