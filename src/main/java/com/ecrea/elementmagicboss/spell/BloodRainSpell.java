package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.BloodRainNeedle;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.util.ParticleHelper;
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

public class BloodRainSpell extends AbstractSpell {

    private static final double SPAWN_HEIGHT = 18.0;

    /** 50%ライフスティール */
    public static final float LIFESTEAL = 0.50f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "blood_rain");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public BloodRainSpell() {
        this.manaCostPerLevel = 1;   // 消費を大幅に抑える
        this.baseSpellPower   = 10;
        this.spellPowerPerLevel = 2;
        this.castTime         = 160;
        this.baseManaCost     = 5;   // 消費を大幅に抑える
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(getRadius(), 1)),
                Component.translatable("ui.elementmagicboss.lifesteal",
                        (int) (LIFESTEAL * 100))
        );
    }

    @Override public CastType getCastType() { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.AMBIENT_CAVE.value());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof BloodRainCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(
                    world,
                    Utils.raycastForEntity(world, entity, 40, true).getLocation(),
                    12);
            playerMagicData.setAdditionalCastData(new BloodRainCastData(targetArea));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    // ---------------------------------------------------------------
    // 毎tick：針を降らせる
    // 160tick × 12.5本/tick = 2000本
    //   偶数tick: 13本 / 奇数tick: 12本 → 80×13 + 80×12 = 2000本
    // ---------------------------------------------------------------
    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        if (playerMagicData == null
                || !(playerMagicData.getAdditionalCastData() instanceof BloodRainCastData castData)) {
            return;
        }

        float radius = getRadius();
        int tick = playerMagicData.getCastDurationRemaining() - 1;

        // 20tickごとに周囲の敵リストを更新
        if (tick % 20 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity,
                    AABB.ofSize(castData.center, radius * 3, radius, radius * 3),
                    e -> e instanceof LivingEntity
                            && !DamageSources.isFriendlyFireBetween(entity, e)));
        }

        // 偶数tick13本・奇数tick12本 = 合計2000本
        int count = (tick % 2 == 0) ? 13 : 12;
        for (int i = 0; i < count; i++) {
            Vec3 center = castData.center;

            Vec3 weightedArea = Vec3.ZERO;
            for (Entity target : castData.trackedEntities) {
                weightedArea = weightedArea.add(
                        target.position().subtract(center)
                                .scale(1f / castData.trackedEntities.size()));
            }

            float spawnRadius = Mth.clampedLerp(radius, radius * .5f,
                    (float) (weightedArea.length() / radius));

            Vec3 spawnTarget = Utils.moveToRelativeGroundLevel(level,
                    center.add(weightedArea).add(
                            new Vec3(0, 0, entity.getRandom().nextFloat() * spawnRadius)
                                    .yRot(entity.getRandom().nextInt(360) * Mth.DEG_TO_RAD)),
                    3).add(0, 0.5, 0);

            Vec3 trajectory = new Vec3(
                    (entity.getRandom().nextDouble() - 0.5) * 0.2,
                    -1.0,
                    (entity.getRandom().nextDouble() - 0.5) * 0.2
            ).normalize();

            Vec3 spawn = Utils.raycastForBlock(level,
                    spawnTarget,
                    spawnTarget.add(trajectory.scale(-SPAWN_HEIGHT)),
                    ClipContext.Fluid.NONE).getLocation().add(trajectory);

            shootNeedle(level, spellLevel, entity, spawn, trajectory);

            MagicManager.spawnParticles(level, ParticleHelper.BLOOD,
                    spawn.x, spawn.y, spawn.z, 3, 0.3, 0.3, 0.3, 0.1, true);
        }
    }

    private void shootNeedle(Level world, int spellLevel, LivingEntity entity,
                              Vec3 spawn, Vec3 trajectory) {
        BloodRainNeedle needle = new BloodRainNeedle(world, entity);
        needle.moveTo(spawn);
        needle.shoot(trajectory);
        needle.setDamage(getDamage(spellLevel, entity));
        world.addFreshEntity(needle);

        world.playSound(null, spawn.x, spawn.y, spawn.z,
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                0.4f, 0.6f + Utils.random.nextFloat() * 0.3f);
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setLifestealPercent(LIFESTEAL)
                .setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.60f; // 2倍 (元: 0.30f)
    }

    private float getRadius() { return 7.0f; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    public static class BloodRainCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public BloodRainCastData(Vec3 center) { this.center = center; }

        @Override
        public void reset() { trackedEntities.clear(); }

        public void updateTrackedEntities(List<Entity> entities) {
            trackedEntities.clear();
            trackedEntities.addAll(entities);
        }
    }
}
