package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.DivineBlessingEffect;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * 神の祝福 (Divine Blessing)
 * 自分自身に神の祝福バフを付与する。
 * - 受ける回復量が multiplier 倍に増幅
 * - 与えるダメージが multiplier 倍に増幅
 * School: Holy / CastType: LONG / Cooldown: 30s / CastTime: 1s
 *
 * Lv1: x2.0 / 10s, LvN: x(2.0+0.5*(N-1)) / (10+4*(N-1))s
 */
public class DivineBlessingSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "divine_blessing");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public DivineBlessingSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;   // 1 second
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float multiplier = DivineBlessingEffect.getMultiplier(spellLevel - 1);
        return List.of(
                Component.translatable("ui.elementmagicboss.divine_blessing_rate",
                        Utils.stringTruncation(multiplier, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        // 自分自身に付与
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.DIVINE_BLESSING.get(),
                getDurationTicks(spellLevel, entity),
                spellLevel - 1,
                false, true, true
        ));
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDurationTicks(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
