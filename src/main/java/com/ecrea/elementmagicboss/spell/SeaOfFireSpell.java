package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.SeaOfFireBomb;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SeaOfFireSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "sea_of_fire");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(12)
            .build();

    public SeaOfFireSpell() {
        this.manaCostPerLevel   = 8;
        this.baseSpellPower     = 8;
        this.spellPowerPerLevel = 3;
        this.castTime           = 20;  // 1秒
        this.baseManaCost       = 60;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(getAoeDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(getRadius(spellLevel, caster), 1))
        );
    }

    @Override public CastType getCastType()             { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()   { return defaultConfig; }
    @Override public ResourceLocation getSpellResource(){ return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.FIRE_BOMB_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.FIRE_BOMB_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CHARGED_CAST;
    }

    // ---------------------------------------------------------------
    // 詠唱完了: 爆弾1発を正面に投射
    // ---------------------------------------------------------------
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        SeaOfFireBomb bomb = new SeaOfFireBomb(level, entity);
        bomb.setPos(entity.position()
                .add(0, entity.getEyeHeight() - bomb.getBoundingBox().getYsize() * .5f, 0)
                .add(entity.getForward()));
        bomb.shoot(entity.getLookAngle());
        bomb.setDeltaMovement(bomb.getDeltaMovement().add(0, 0.2, 0));
        bomb.setExplosionRadius(getRadius(spellLevel, entity));
        bomb.setDamage(getDamage(spellLevel, entity));
        bomb.setAoeDamage(getAoeDamage(spellLevel, entity));
        level.addFreshEntity(bomb);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** 爆発半径: MagmaBomb(3 + modifier) の3倍 */
    public float getRadius(int spellLevel, LivingEntity caster) {
        return (3 + getEntityPowerMultiplier(caster)) * 3.0f;
    }

    /** 直撃ダメージ: MagmaBombの3倍 */
    public float getDamage(int spellLevel, LivingEntity caster) {
        return baseSpellPower * getEntityPowerMultiplier(caster) * 3.0f;
    }

    /** 範囲継続ダメージ: MagmaBombの3倍 */
    public float getAoeDamage(int spellLevel, LivingEntity caster) {
        return (1 + getSpellPower(spellLevel, caster) * 0.1f) * 3.0f;
    }
}
