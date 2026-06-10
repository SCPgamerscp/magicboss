package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
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
 * リミットブレイク (Limit Break)
 * 自分自身にリミットブレイクバフを付与する。
 * - 詠唱時間短縮 +100% (最大)
 * - クールダウン短縮 +100% (最大)
 * - マナ再生 +100% (最大)
 * School: Evocation / CastType: INSTANT / Cooldown: 60s
 */
public class LimitBreakSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "limit_break");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(60)
            .build();

    public LimitBreakSpell() {
        this.manaCostPerLevel   = 0;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 0;   // INSTANT
        this.baseManaCost       = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.LIMIT_BREAK.get(),
                getDurationTicks(spellLevel, entity),
                0,          // amplifier は 0 固定 (AttributeModifier は effect に内蔵)
                false, true, true
        ));
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDurationTicks(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
