package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.CrimsonYoungMoonEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class CrimsonYoungMoonSpell extends AbstractSpell {
    public static final int DURATION_TICKS = 20 * 20;
    public static final int BURST_INTERVAL_TICKS = 5;
    public static final int PROJECTILES_PER_BURST = 100;
    public static final float MOON_HEIGHT = 10.0f;
    public static final float LIFESTEAL = 2.0f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "crimson_young_moon");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public CrimsonYoungMoonSpell() {
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getProjectileDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(DURATION_TICKS, 1)),
                Component.translatable("ui.elementmagicboss.lifesteal", (int) (LIFESTEAL * 100)),
                Component.translatable("ui.elementmagicboss.fire_frying_pan_burst",
                        PROJECTILES_PER_BURST, BURST_INTERVAL_TICKS)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
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
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.RESPAWN_ANCHOR_CHARGE);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide()) {
            CrimsonYoungMoonEntity moon = new CrimsonYoungMoonEntity(level, entity, getProjectileDamage(spellLevel, entity));
            moon.setPos(entity.getX(), entity.getY() + MOON_HEIGHT, entity.getZ());
            level.addFreshEntity(moon);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getProjectileDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.6f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker)
                .setLifestealPercent(LIFESTEAL)
                .setIFrames(0);
    }
}
