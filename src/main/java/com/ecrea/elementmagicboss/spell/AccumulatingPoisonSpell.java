package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.AccumulatingPoisonEffect;
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
 * 蓄積する毒 (Accumulating Poison)
 * ターゲットに蓄積する毒デバフを付与する。
 * デバフ中に毒を受けると、毒の持続時間が上書きされず加算される。
 * 毒ダメージも multiplier 倍に増幅される。
 * School: Nature / CastType: LONG / Cooldown: 30s / CastTime: 1s
 *
 * Lv1: x2.0 / 10s, Lv2: x2.5 / 14s, LvN: x(2.0+0.5*(N-1)) / (10+4*(N-1))s
 */
public class AccumulatingPoisonSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "accumulating_poison");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public AccumulatingPoisonSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;   // 1 second
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float multiplier = AccumulatingPoisonEffect.getMultiplier(spellLevel - 1);
        return List.of(
                Component.translatable("ui.elementmagicboss.accumulating_poison_rate",
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
        return Optional.of(SoundEvents.SLIME_SQUISH);
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
                        ModMobEffects.ACCUMULATING_POISON.get(),
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
