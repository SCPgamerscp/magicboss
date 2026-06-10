package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.projectile.HeartOrbitEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class HealingDeterminationSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "healing_determination");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public HealingDeterminationSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 20;
        this.spellPowerPerLevel = 5;
        this.castTime           = 0;
        this.baseManaCost       = 60;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDuration(spellLevel, caster), 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.AMETHYST_BLOCK_RESONATE);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        int duration = getDuration(spellLevel, entity);

        // Apply buff (amplifier = spellLevel - 1)
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.HEALING_DETERMINATION.get(),
                duration, spellLevel - 1, false, true, true));

        // Spawn 6 orbiting hearts
        if (world instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 6; i++) {
                double baseAngle = (Math.PI * 2.0 / 6.0) * i;
                HeartOrbitEntity heart = new HeartOrbitEntity(world, entity, i + 1, baseAngle);
                serverLevel.addFreshEntity(heart);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
