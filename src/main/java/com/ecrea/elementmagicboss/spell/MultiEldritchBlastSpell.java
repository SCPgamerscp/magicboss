package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.eldritch_blast.EldritchBlastVisualEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 多段エルドリッチブラスト
 *
 * 詠唱者の周囲にクジャク扇状にCANNON_COUNT個の砲台を展開し、
 * 毎tick全砲台から同時に1発ずつエルドリッチブラストを発射する。
 * 25砲台 × 20tick/秒 = 500発/秒、20秒間継続。
 *
 * 各砲台は独立したエルドリッチブラストの動作：
 *   砲台位置→ターゲット方向 raycast → EldritchBlastVisualEntity → damage → UNSTABLE_ENDERパーティクル
 */
public class MultiEldritchBlastSpell extends AbstractSpell {

    /** 扇状砲台数（10砲台 × 20tick = 200発/秒） */
    private static final int   CANNON_COUNT = 10;
    /** 砲台展開半径（詠唱者中心からの距離） */
    private static final float FAN_RADIUS   = 3.0f;
    /** レイキャスト射程（EldritchBlastと同じ30） */
    private static final float RANGE        = 30.0f;
    /** 20秒 = 400tick */
    private static final int   CAST_TICKS   = 400;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "multi_eldritch_blast");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(12)
            .build();

    public MultiEldritchBlastSpell() {
        this.manaCostPerLevel   = 1;
        this.baseSpellPower     = 10;
        this.spellPowerPerLevel = 2;
        this.castTime           = CAST_TICKS;
        this.baseManaCost       = 1;
    }

    @Override public int getCastTime(int spellLevel) { return CAST_TICKS; }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", CANNON_COUNT * 20)
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() { return Optional.empty(); }
    @Override public Optional<SoundEvent> getCastFinishSound() { return Optional.empty(); }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        float damage  = getDamage(spellLevel, entity);

        // ターゲット位置をlookAngleで取得（EldritchBlastと同じ）
        HitResult primaryHit = Utils.raycastForEntity(level, entity, RANGE, true, 0.15f);
        Vec3 targetPos = primaryHit.getLocation();

        // lookDir垂直2軸（扇形範囲を構成する基底ベクトル）
        Vec3 look  = entity.getLookAngle().normalize();
        Vec3 up    = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up);
        if (right.lengthSqr() < 1e-6) right = look.cross(new Vec3(1, 0, 0));
        right      = right.normalize();
        Vec3 fanUp = right.cross(look).normalize();

        java.util.Random rng = new java.util.Random();

        // 毎tick CANNON_COUNT 発を扇形範囲内ランダム位置から同時発射
        for (int i = 0; i < CANNON_COUNT; i++) {
            // 扇形範囲内のランダム点（right・fanUp平面上でランダム角度・距離）
            double angle    = rng.nextDouble() * 2 * Math.PI;
            double distance = rng.nextDouble() * FAN_RADIUS;
            Vec3 offset     = right.scale(Math.cos(angle) * distance)
                                   .add(fanUp.scale(Math.sin(angle) * distance));

            // 砲台起点（EldritchBlastと同じくeye位置から-0.75Y）
            Vec3 cannonOrigin = entity.getEyePosition().add(offset).subtract(0, 0.75, 0);

            // 砲台→ターゲット方向（それぞれ異なる角度からターゲットへ向かう）
            Vec3 dir      = targetPos.subtract(cannonOrigin).normalize();
            Vec3 cannonEnd = cannonOrigin.add(dir.scale(RANGE));

            // ===== EldritchBlastSpell.onCast と全く同じ処理 =====
            HitResult hit = Utils.raycastForEntity(
                    level, entity, cannonOrigin, cannonEnd, true, 0.15f, Utils::canHitWithRaycast);
            Vec3 hitPos = hit.getLocation();

            level.addFreshEntity(new EldritchBlastVisualEntity(level, cannonOrigin, hitPos, entity));

            // 各発射ごとに音を鳴らす（EldritchBlastのfinishサウンドと同じ）
            level.playSound(null, cannonOrigin.x, cannonOrigin.y, cannonOrigin.z,
                    SoundRegistry.ELDRITCH_BLAST.get(), SoundSource.PLAYERS,
                    0.6f, 0.8f + level.random.nextFloat() * 0.4f);

            if (hit.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) hit).getEntity();
                if (target.canBeHitByProjectile()) {
                    DamageSources.applyDamage(target, damage, getDamageSource(entity));
                    MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER,
                            hitPos.x, hitPos.y, hitPos.z, 50, 0, 0, 0, .3, false);
                }
            }
        }
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    /** EldritchBlastのgetDamage(spellPower)×20倍 */
    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.0f;
    }
}
