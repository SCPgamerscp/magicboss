package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

/**
 * 無敵
 * 詠唱者に「無敵」マーカーバフを付与。
 * バフ中、9秒(180tick)ごとに各種バフを10秒(200tick)付与する。
 * School: Eldritch / Cooldown: 20s / CastType: INSTANT
 */
public class InvincibilitySpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "invincibility");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public InvincibilitySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 100;
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
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        int duration = getDuration(spellLevel, entity);

        // 無敵マーカーエフェクトを付与
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.INVINCIBILITY.get(), duration, 0, false, true, true));

        // キャスト直後に即座にバフを一度付与（isDurationEffectTick は duration%180==0 のため
        // 端数によっては最初の発火まで時間がかかる可能性があるため）
        applySubEffects(entity);

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }

    private void applySubEffects(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ABYSSAL_SHROUD.get(),   200,  0, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ECHOING_STRIKES.get(),  200, 29, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.HASTENED.get(),         200,  4, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.CHARGED.get(),          200,  4, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.THUNDERSTORM.get(),     200, 29, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.OAKSKIN.get(),          200, 19, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SPIDER_ASPECT.get(),    200, 19, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,                  200,  4, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,             200,  4, false, true, true));
        entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,                  200,  4, false, true, true));
    }
}
