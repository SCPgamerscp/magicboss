package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.poison_arrow.PoisonArrow;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 毒矢嵐 (Poison Arrow Storm)
 * flying_blood_slash の Poison Arrow 版。
 * 全方位にランダムな方向で Poison Arrow を放つ。
 * 毎tick 10本、10秒間（200tick）継続。
 * School: Nature / CastType: CONTINUOUS / Cooldown: 10s
 */
public class PoisonArrowStormSpell extends AbstractSpell {

    private static final int CAST_TICKS    = 200;   // 10秒
    private static final int ARROWS_PER_TICK = 10;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "poison_arrow_storm");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public PoisonArrowStormSpell() {
        this.manaCostPerLevel   = 3;
        this.baseSpellPower     = 5;
        this.spellPowerPerLevel = 1;
        this.castTime           = CAST_TICKS;
        this.baseManaCost       = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getArrowDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(getAoeDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count",
                        CAST_TICKS * ARROWS_PER_TICK)
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.POISON_ARROW_CHARGE.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        Vec3 origin = entity.getEyePosition().add(entity.getLookAngle().normalize().scale(0.4f));
        float damage    = getArrowDamage(spellLevel, entity);
        float aoeDamage = getAoeDamage(spellLevel, entity);
        RandomSource random = entity.getRandom();

        for (int i = 0; i < ARROWS_PER_TICK; i++) {
            Vec3 direction = randomUnitVector(random);
            PoisonArrow arrow = new PoisonArrow(level, entity);
            arrow.setPos(origin);
            arrow.shoot(direction);
            arrow.setDamage(damage);
            arrow.setAoeDamage(aoeDamage);
            level.addFreshEntity(arrow);
        }
    }

    public float getArrowDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }

    public float getAoeDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.185f;
    }

    /** 全方位均一ランダム単位ベクトル */
    private static Vec3 randomUnitVector(RandomSource random) {
        double phi      = random.nextDouble() * (Math.PI * 2.0);
        double cosTheta = random.nextDouble() * 2.0 - 1.0;
        double sinTheta = Math.sqrt(1.0 - cosTheta * cosTheta);
        return new Vec3(Math.cos(phi) * sinTheta, cosTheta, Math.sin(phi) * sinTheta);
    }
}
