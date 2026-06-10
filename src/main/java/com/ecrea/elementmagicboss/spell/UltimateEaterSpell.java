package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
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
 * 全てをむさぼり食うもの (アルティメットイーター)
 * Gluttonyの強化版バフ。
 * - 食事時マナ回復量がGluttonyの3倍
 * - 回復力が3倍 (ModEventsのLivingHealEventで処理)
 * - 持続時間がGluttonyの3倍 (1800tick=90秒)
 * School: Nature / CastType: INSTANT / Cooldown: 20s
 */
public class UltimateEaterSpell extends AbstractSpell {

    /** Gluttony baseSpellPower=30 → duration=600tick。その3倍 */
    private static final int DURATION_TICKS = 1800;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "ultimate_eater");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

    public UltimateEaterSpell() {
        this.baseManaCost       = 50;
        this.manaCostPerLevel   = 0;
        this.baseSpellPower     = 30;
        this.spellPowerPerLevel = 0;
        this.castTime           = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        // マナ回復倍率表示: Gluttonyと同形式で3倍の値
        float ratio = (4 + (spellLevel - 1)) * 0.5f * 3.0f;
        return List.of(
                Component.translatable("ui.irons_spellbooks.mana_recovery",
                        Utils.stringTruncation(ratio, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(DURATION_TICKS, 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.PLAYER_BURP);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.ULTIMATE_EATER.get(),
                DURATION_TICKS,
                spellLevel - 1,  // amplifier = spellLevel - 1 (Gluttonyと同じスケール)
                false, true, true
        ));
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
