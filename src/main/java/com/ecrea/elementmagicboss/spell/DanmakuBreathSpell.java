package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.DanmakuShotProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 弾幕ブレス (Danmaku Breath)
 * 1バーストに100発の弾幕をブレス状に噴射し続ける。合計15秒。
 */
public class DanmakuBreathSpell extends AbstractSpell {

    public static final int  DANMAKU_PER_BURST = 100;
    private static final int FIRE_INTERVAL     = 5;    // 5tickごとに1バースト
    private static final int CAST_TICKS        = 200;  // 10秒固定
    /** ブレス拡散半角(ラジアン) ≒ 10度 */
    private static final double SPREAD_ANGLE   = Math.toRadians(10);

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_breath");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public DanmakuBreathSpell() {
        this.manaCostPerLevel   = 1;  // 1/3 (元: 2)
        this.baseSpellPower     = 4;
        this.spellPowerPerLevel = 1;
        this.castTime           = CAST_TICKS;
        this.baseManaCost       = 2;  // 1/3 (元: 5)
    }

    @Override
    public int getCastTime(int spellLevel) { return CAST_TICKS; }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", DANMAKU_PER_BURST)
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ENDER_DRAGON_GROWL);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        if (playerMagicData == null) return;
        if ((playerMagicData.getCastDurationRemaining() + 1) % FIRE_INTERVAL != 0) return;

        shootDanmakuBurst(level, spellLevel, entity);
    }

    private void shootDanmakuBurst(Level level, int spellLevel, LivingEntity entity) {
        Vec3 look   = entity.getLookAngle();
        Vec3 origin = entity.getEyePosition().add(look.normalize().scale(0.5));
        float damage = getDamage(spellLevel, entity);
        String spellIdStr = spellId.toString();
        Random rng = new Random();

        for (int i = 0; i < DANMAKU_PER_BURST; i++) {
            // ブレス拡散: 球面上のランダムオフセットをSPREAD_ANGLE以内でルックアングルに加算
            Vec3 dir = randomSpread(look, rng);

            DanmakuShotProjectile projectile = new DanmakuShotProjectile(level, entity);
            projectile.setPos(origin);
            projectile.shoot(dir);
            projectile.setDamage(damage);
            projectile.setDamageSpellId(spellIdStr);
            projectile.setVariant(rng.nextInt(DanmakuShotSpell.PELLET_COUNT)); // ランダムカラー
            level.addFreshEntity(projectile);
        }

        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS,
                1.5f, 0.8f + Utils.random.nextFloat() * 0.4f);
    }

    /**
     * lookDir を中心に SPREAD_ANGLE 以内のランダム方向を返す。
     * 球面座標でランダムな極角[0, spread]・方位角[0, 2π)を生成し、
     * lookDir を Z 軸とした局所座標系に変換する。
     */
    private Vec3 randomSpread(Vec3 look, Random rng) {
        double theta = Math.acos(1.0 - rng.nextDouble() * (1.0 - Math.cos(SPREAD_ANGLE)));
        double phi   = rng.nextDouble() * Math.PI * 2;

        // lookDir に対して垂直な2軸を生成
        Vec3 perp1 = look.cross(new Vec3(0, 1, 0));
        if (perp1.lengthSqr() < 1e-6) perp1 = look.cross(new Vec3(1, 0, 0));
        perp1 = perp1.normalize();
        Vec3 perp2 = look.cross(perp1).normalize();

        return look.scale(Math.cos(theta))
                   .add(perp1.scale(Math.sin(theta) * Math.cos(phi)))
                   .add(perp2.scale(Math.sin(theta) * Math.sin(phi)))
                   .normalize();
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f; // 3倍 (元: 0.5f)
    }
}
