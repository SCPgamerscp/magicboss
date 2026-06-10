package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
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

public class TrueBlazingStormSpell extends AbstractSpell {

    /** 1回の発射で出るFireballの数 */
    public static final int FIREBALL_COUNT = 20;

    /** 何tickごとに1セット発射するか（BlazeStormと同じ5tick） */
    private static final int FIRE_INTERVAL = 5;

    /**
     * ショットガン拡散の不正確さ係数。
     * SmallMagicFireball.shoot(Vec3, float inaccuracy) に渡す値。
     * tan(10°) ≈ 0.176 → 約10度の拡散半径に相当。
     */
    private static final float SHOTGUN_INACCURACY = 0.18f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_blazing_storm");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public TrueBlazingStormSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower   = 8;
        this.spellPowerPerLevel = 1;
        this.castTime         = 60 - 5;
        this.baseManaCost     = 3;
    }

    /** BlazeStorm と同じくレベルで詠唱時間が伸びる */
    @Override
    public int getCastTime(int spellLevel) {
        return castTime + 5 * spellLevel;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", FIREBALL_COUNT)
        );
    }

    @Override public CastType getCastType()           { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BLAZE_AMBIENT);
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

    // ---------------------------------------------------------------
    // 毎FIRE_INTERVALtick：20発のSmallFireballをショットガン状に一斉射出（10度拡散）
    // ---------------------------------------------------------------
    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        if (playerMagicData == null) return;
        if ((playerMagicData.getCastDurationRemaining() + 1) % FIRE_INTERVAL != 0) return;

        shootFireballBurst(level, spellLevel, entity);
    }

    private void shootFireballBurst(Level world, int spellLevel, LivingEntity entity) {
        Vec3 look   = entity.getLookAngle();
        Vec3 origin = entity.getEyePosition().add(look.normalize().scale(0.4f));
        float damage = getDamage(spellLevel, entity);

        for (int i = 0; i < FIREBALL_COUNT; i++) {
            // irons_spellbooks:small_fireball をそのまま使用
            // shoot(Vec3, float inaccuracy) が内部で3D球面ランダムオフセットを加算 → 10度拡散
            SmallMagicFireball fireball = new SmallMagicFireball(world, entity);
            fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
            fireball.shoot(look, SHOTGUN_INACCURACY);
            fireball.setDamage(damage);
            world.addFreshEntity(fireball);
        }

        world.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS,
                2.0f, 0.9f + Utils.random.nextFloat() * 0.2f);
    }

    /** 着火40tick、iフレームなし（BlazeStormと同じ） */
    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setFireTicks(40)
                .setIFrames(0);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.6f;
    }
}
