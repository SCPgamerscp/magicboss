package com.ecrea.elementmagicboss.entity.projectile;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.ArrowVolleyEntity;
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Supplier;

public class TrueArrowVolleyEntity extends ArrowVolleyEntity {
    private static final int DELAY = 5;
    private static final float MOTION_SPEED = 0.85f;
    private static final float RANDOM_SPREAD = 0.04f;
    private static final float ARROW_SPACING = 0.15f;
    private static final float ROTATION_STEP = 7f;

    private int rows;
    private int arrowsPerRow;

    public TrueArrowVolleyEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (tickCount % DELAY == 0) {
                fireVolleyRow();
            } else if (tickCount > rows * DELAY) {
                discard();
            }
        }
    }

    private void fireVolleyRow() {
        Vec3 motion = Vec3.directionFromRotation(getXRot() - tickCount / 5f * ROTATION_STEP, this.getYRot())
                .normalize()
                .scale(MOTION_SPEED);
        Vec3 orthogonal = new Vec3(
                -Mth.cos(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI),
                0,
                Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI)
        );

        for (int i = 0; i < arrowsPerRow; i++) {
            float distance = (i - (arrowsPerRow - 1) * 0.5f) * ARROW_SPACING;
            SmallMagicArrow arrow = new SmallMagicArrow(level(), this.getOwner());
            arrow.setDamage(this.getDamage());

            Vec3 spawn = this.position().add(orthogonal.scale(distance));
            arrow.setPos(spawn);
            arrow.shoot(motion.add(Utils.getRandomVec3(RANDOM_SPREAD)));
            arrow.setOwner(this.getOwner());
            level().addFreshEntity(arrow);
            MagicManager.spawnParticles(level(), ParticleTypes.FIREWORK, spawn.x, spawn.y, spawn.z,
                    2, .1, .1, .1, .05, false);
        }

        level().playSound(null, position().x, position().y, position().z,
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.0f,
                1.1f + Utils.random.nextFloat() * .3f);
        level().playSound(null, position().x, position().y, position().z,
                SoundRegistry.BOW_SHOOT.get(), SoundSource.NEUTRAL, 2,
                Utils.random.nextIntBetweenInclusive(16, 20) * .1f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("rows", rows);
        tag.putInt("arrowsPerRow", arrowsPerRow);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.rows = tag.getInt("rows");
        this.arrowsPerRow = tag.getInt("arrowsPerRow");
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setArrowsPerRow(int arrowsPerRow) {
        this.arrowsPerRow = arrowsPerRow;
    }

    @Override
    public void trailParticles() {
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }
}
