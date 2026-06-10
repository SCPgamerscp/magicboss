package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.EternalFireEffect;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
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

/**
 * 永遠の火 (Eternal Fire)
 * ターゲットに永遠の火デバフを付与する。
 * - デバフ中に炎上すると 2147483647 tick 燃え続ける
 * - 消火不可
 * - 炎上ダメージが multiplier 倍になる
 * School: Fire / CastType: LONG / Cooldown: 20s / CastTime: 1s
 */
public class EternalFireSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "eternal_fire");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public EternalFireSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;   // 1 second
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float multiplier = EternalFireEffect.getMultiplier(spellLevel - 1);
        return List.of(
                Component.translatable("ui.elementmagicboss.eternal_fire_damage",
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
        return Optional.of(SoundEvents.FIRECHARGE_USE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BLAZE_SHOOT);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel,
                                          LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 32, 0.35f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData
                && world instanceof ServerLevel serverLevel) {
            LivingEntity target = targetData.getTarget(serverLevel);
            if (target != null) {
                target.addEffect(new MobEffectInstance(
                        ModMobEffects.ETERNAL_FIRE.get(),
                        getDurationTicks(spellLevel, entity),
                        spellLevel - 1,
                        false, true, true
                ));
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDurationTicks(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
