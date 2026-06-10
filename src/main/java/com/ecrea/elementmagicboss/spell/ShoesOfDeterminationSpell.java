package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.projectile.ShoeEntity;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ShoesOfDeterminationSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "shoes_of_determination");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public ShoesOfDeterminationSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower   = 20;
        this.spellPowerPerLevel = 5;
        this.castTime         = 0;
        this.baseManaCost     = 80;
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
        return Optional.of(SoundEvents.ARMOR_EQUIP_LEATHER);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        int duration = getDuration(spellLevel, entity);

        // Buff + Speed 5
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.SHOES_OF_DETERMINATION.get(), duration, 0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 4, false, true, true));

        if (world instanceof ServerLevel serverLevel) {
            // 3x density: spellLevel * 12
            int count  = spellLevel * 12;
            float dmg  = getSpellPower(spellLevel, entity) / 3.0f;
            Random rng = new Random();

            for (int i = 0; i < count; i++) {
                // Evenly distribute angles with slight jitter
                double angle = (Math.PI * 2.0 * i) / count
                             + (rng.nextDouble() - 0.5) * (Math.PI * 2.0 / count) * 0.5;
                // Radius: 2 ~ 10
                double r     = 2.0 + rng.nextDouble() * 8.0;
                double ox    = Math.cos(angle) * r;
                double oz    = Math.sin(angle) * r;
                // Random phase offset 0 ~ 2*PI for independent bobbing
                double phase = rng.nextDouble() * Math.PI * 2.0;

                ShoeEntity shoe = new ShoeEntity(world, entity, dmg, ox, oz, phase);
                serverLevel.addFreshEntity(shoe);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
