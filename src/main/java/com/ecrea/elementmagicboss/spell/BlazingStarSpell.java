package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.BlazingStarMeteorProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlazingStarSpell extends AbstractSpell {
    public static final int BARRAGE_COUNT = 10;
    public static final int METEORS_PER_SPAWN = 10;

    private final ResourceLocation spellId = new ResourceLocation(ElementMagicBossMod.MOD_ID, "blazing_star");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public BlazingStarSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 120;
        this.baseManaCost = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getShardDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", BARRAGE_COUNT)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ENDER_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof BlazingStarCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(world, Utils.raycastForEntity(world, entity, 40, true).getLocation(), 12);
            playerMagicData.setAdditionalCastData(new BlazingStarCastData(targetArea));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || !(playerMagicData.getAdditionalCastData() instanceof BlazingStarCastData castData)) {
            return;
        }

        float radius = getRadius();
        int tick = playerMagicData.getCastDurationRemaining() - 1;
        if (tick % 20 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity,
                    AABB.ofSize(castData.center, radius * 3, radius, radius * 3),
                    target -> target instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(entity, target)));
        }

        if (tick % 6 == 0) {
            Vec3 center = castData.center;
            Vec3 weightedArea = Vec3.ZERO;
            if (!castData.trackedEntities.isEmpty()) {
                for (Entity target : castData.trackedEntities) {
                    weightedArea = weightedArea.add(target.position().subtract(center).scale(1f / castData.trackedEntities.size()));
                }
            }
            float spawnRadius = (float) Mth.clampedLerp(radius, radius * 0.5f, weightedArea.length() / radius);
            for (int i = 0; i < METEORS_PER_SPAWN; i++) {
                Vec3 spawnTarget = Utils.moveToRelativeGroundLevel(level,
                        center.add(weightedArea)
                                .add(new Vec3(0, 0, entity.getRandom().nextFloat() * spawnRadius).yRot(entity.getRandom().nextInt(360) * Mth.DEG_TO_RAD)),
                        3).add(0, 0.5, 0);
                Vec3 trajectory = new Vec3(0.15f, -0.85f, 0.0f).normalize();
                Vec3 spawn = Utils.raycastForBlock(level, spawnTarget, spawnTarget.add(trajectory.scale(-12)), ClipContext.Fluid.NONE).getLocation().add(trajectory);
                shootMeteor(level, spellLevel, entity, spawn, trajectory);
            }
        }
    }

    private void shootMeteor(Level world, int spellLevel, LivingEntity caster, Vec3 spawn, Vec3 trajectory) {
        BlazingStarMeteorProjectile meteor = new BlazingStarMeteorProjectile(world, caster);
        meteor.setPos(spawn.add(-1.0D, 0.0D, 0.0D));
        meteor.shoot(trajectory, 0.075F);
        meteor.setDamage(getImpactDamage(spellLevel, caster));
        meteor.setShardDamage(getShardDamage(spellLevel, caster));
        meteor.setExplosionRadius(2.5F);
        world.addFreshEntity(meteor);
        world.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 2.5f, 0.8f + Utils.random.nextFloat() * 0.2f);
    }

    private float getImpactDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 5.0f;
    }

    private float getShardDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.2f;
    }

    private float getRadius() {
        return 6.0f;
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    public static class BlazingStarCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public BlazingStarCastData(Vec3 center) {
            this.center = center;
        }

        @Override
        public void reset() {
            trackedEntities.clear();
        }

        public void updateTrackedEntities(List<Entity> entities) {
            trackedEntities.clear();
            trackedEntities.addAll(entities);
        }
    }
}