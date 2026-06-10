package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.projectile.SpinningSwordEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;

/**
 * 剣の舞 (Ken no Mai)
 * 詠唱時に半径10ブロック以内のランダムな位置に200本の剣を散布する
 * エフェクト有効中は剣がオーナーに追従し続け、消えたら一斉消滅
 */
public class KenNoMaiSpell extends AbstractSpell {

    public static final int   SWORD_COUNT   = 200;
    public static final float SPREAD_RADIUS = 10.0f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "ken_no_mai");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public KenNoMaiSpell() {
        this.manaCostPerLevel   = 4;
        this.baseSpellPower     = 6;
        this.spellPowerPerLevel = 1;
        this.castTime           = 0;
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getSwordDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.strikes", SWORD_COUNT)
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        // 1) エフェクト付与 (マーカーとして使用)
        int amplifier = getAmplifier(spellLevel, entity);
        entity.addEffect(new MobEffectInstance(
                ModMobEffects.KEN_NO_MAI.get(),
                getDurationTicks(spellLevel, entity),
                amplifier,
                false, false, true
        ));

        // 2) サーバーサイドでのみ200本の剣をランダム散布
        if (level instanceof ServerLevel serverLevel) {
            float damage = getSwordDamage(spellLevel, entity);
            Random rng = new Random();

            for (int i = 0; i < SWORD_COUNT; i++) {
                // 均一円形分布: sqrt(random) で面積的に均一なランダム配置
                double angle    = rng.nextDouble() * Math.PI * 2;
                double distance = Math.sqrt(rng.nextDouble()) * SPREAD_RADIUS;
                float ox = (float) (Math.cos(angle) * distance);
                float oz = (float) (Math.sin(angle) * distance);

                SpinningSwordEntity sword = new SpinningSwordEntity(
                        serverLevel, entity, damage, ox, oz);
                serverLevel.addFreshEntity(sword);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    // iFrames=0 で毎tick確実にダメージ
    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    private int getAmplifier(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) - 1);
    }

    public float getSwordDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.0f; // 2倍 (元: 0.5f)
    }

    public int getDurationTicks(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
