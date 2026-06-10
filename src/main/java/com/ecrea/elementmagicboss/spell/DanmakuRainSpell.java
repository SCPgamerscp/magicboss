package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.WaterDanmakuProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
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

/**
 * 弾幕雨 (Danmaku Rain)
 * Starfallの弾幕版。空から水の弾幕を降らせる。
 * 弾幕は無敵時間無視。命中した相手に「濡れ」デバフを与える。
 * 濡れ: 呪文耐性・氷呪文耐性・雷呪文耐性を大きく低下。30秒持続。
 * School: Ice / CastType: CONTINUOUS / Cooldown: 20s
 */
public class DanmakuRainSpell extends AbstractSpell {

    /** 弾幕タグ（ModEventsで命中時デバフ付与に使用） */
    public static final String RAIN_TAG = "danmaku_rain";

    private static final float RADIUS = 8f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_rain");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public DanmakuRainSpell() {
        this.manaCostPerLevel   = 1;
        this.baseSpellPower     = 10;
        this.spellPowerPerLevel = 2;
        this.castTime           = 400;  // 20秒
        this.baseManaCost       = 5;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        int resistReduction = spellLevel * 10;  // Lv1=10%, Lv10=100%
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.elementmagicboss.wet_resist_reduction", resistReduction),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(600, 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.AMBIENT_UNDERWATER_ENTER);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof DanmakuRainCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(world,
                    Utils.raycastForEntity(world, entity, 40, true).getLocation(), 12);
            playerMagicData.setAdditionalCastData(new DanmakuRainCastData(targetArea));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        if (playerMagicData == null
                || !(playerMagicData.getAdditionalCastData() instanceof DanmakuRainCastData castData)) {
            return;
        }

        float radius = RADIUS;
        int tick = playerMagicData.getCastDurationRemaining() - 1;

        // 20tickごとにターゲットリストを更新
        if (tick % 20 == 0) {
            castData.updateTrackedEntities(level.getEntities(entity,
                    AABB.ofSize(castData.center, radius * 3, radius, radius * 3),
                    e -> e instanceof LivingEntity && !DamageSources.isFriendlyFireBetween(entity, e)));
        }

        // 4tickごとに弾幕を20発降らせる（元の10倍）
        if (tick % 4 == 0) {
            for (int i = 0; i < 20; i++) {
                Vec3 center = castData.center;
                Vec3 weightedArea = Vec3.ZERO;
                for (Entity target : castData.trackedEntities) {
                    weightedArea = weightedArea.add(
                            target.position().subtract(center).scale(1f / castData.trackedEntities.size()));
                }
                float spawnRadius = Mth.clampedLerp(radius, radius * .5f,
                        (float) weightedArea.length() / radius);
                Vec3 spawnTarget = Utils.moveToRelativeGroundLevel(level,
                        center.add(weightedArea).add(new Vec3(0, 0,
                                entity.getRandom().nextFloat() * spawnRadius)
                                .yRot(entity.getRandom().nextInt(360) * Mth.DEG_TO_RAD)), 3)
                        .add(0, 0.5, 0);
                Vec3 trajectory = new Vec3(.1f, -.9f, 0).normalize();
                Vec3 spawn = Utils.raycastForBlock(level, spawnTarget,
                        spawnTarget.add(trajectory.scale(-14)), ClipContext.Fluid.NONE)
                        .getLocation().add(trajectory);
                shootDanmaku(level, spellLevel, entity, spawn, trajectory);
                // 水のパーティクル
                MagicManager.spawnParticles(level, ParticleTypes.DRIPPING_WATER,
                        spawn.x, spawn.y, spawn.z, 3, 0.1, 0.1, 0.1, 0, false);
            }
        }
    }

    private void shootDanmaku(Level level, int spellLevel, LivingEntity entity,
                               Vec3 spawn, Vec3 trajectory) {
        WaterDanmakuProjectile bullet = new WaterDanmakuProjectile(level, entity);
        bullet.setPos(spawn);
        bullet.shoot(trajectory);
        bullet.setDamage(getDamage(spellLevel, entity));
        bullet.setVariant(entity.getRandom().nextInt(13));  // 色とりどり
        bullet.addTag(RAIN_TAG);
        bullet.addTag(RAIN_TAG + "_lv" + spellLevel);  // デバフレベル情報を埋め込む
        level.addFreshEntity(bullet);
        level.playSound(null, spawn.x, spawn.y, spawn.z,
                SoundEvents.WEATHER_RAIN, SoundSource.PLAYERS,
                0.5f, 1.2f + entity.getRandom().nextFloat() * 0.3f);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.5f;  // 元の1/4 (2.0f → 0.5f)
    }

    /** CastData: Starfallと同様のターゲット追跡 */
    public static class DanmakuRainCastData implements ICastData {
        Vec3 center;
        final List<Entity> trackedEntities = new ArrayList<>();

        public DanmakuRainCastData(Vec3 center) {
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
