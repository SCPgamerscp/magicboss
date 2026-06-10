package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.FreezeMultiplyEffect;
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
 * 蜃咲ｵ仙榊喧 (Freeze Multiply)
 * 繧ｿ繝ｼ繧ｲ繝・ヨ縺ｫ蜃咲ｵ仙榊喧繝・ヰ繝輔ｒ荳弱∴繧九・ * School: Ice / CastType: LONG / Cooldown: 20s / CastTime: 1s
 *
 *
 * 蜉ｹ譫・
 *
 * - 蜃咲ｵ舌ム繝｡繝ｼ繧ｸ縺・multiplier 蛟阪↓蠅怜ｹ・ * - 遘ｻ蜍暮溷ｺｦ縺・80% 菴惹ｸ・ */
public class FreezeMultiplySpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "freeze_multiply");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public FreezeMultiplySpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 20;   // 1 second
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        float multiplier = FreezeMultiplyEffect.getMultiplier(spellLevel - 1);
        return List.of(
                Component.translatable("ui.elementmagicboss.freeze_multiply_rate",
                        Utils.stringTruncation(multiplier, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.freeze_multiply_slow", 80)
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.POWDER_SNOW_FALL);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.GLASS_BREAK);
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
                int amplifier = spellLevel - 1;
                int duration  = getDurationTicks(spellLevel, entity);
                target.addEffect(new MobEffectInstance(
                        ModMobEffects.FREEZE_MULTIPLY.get(),
                        duration, amplifier,
                        false, true, true
                ));
                // Instantly freeze for Integer.MAX_VALUE ticks (effectively permanent)
                target.setTicksFrozen(2147483647);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getDurationTicks(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
