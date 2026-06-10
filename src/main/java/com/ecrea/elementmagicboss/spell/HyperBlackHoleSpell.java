package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.HyperBlackHoleMissile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * 超大質量ブラックホール
 * マジックミサイルを発射し、着弾地点にブラックホールを生成する。
 * 半径30・1tick50回・無敵時間無視。
 */
public class HyperBlackHoleSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "hyper_black_hole");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public HyperBlackHoleSpell() {
        this.manaCostPerLevel  = 5;
        this.baseSpellPower    = 10;
        this.spellPowerPerLevel = 2;
        this.castTime          = 0;
        this.baseManaCost      = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.hyper_bh_damage",
                        String.format("%.1f", getDamage(spellLevel, caster)))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.BLACK_HOLE_CAST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            float damage = getDamage(spellLevel, entity);
            Vec3 look = entity.getLookAngle();
            Vec3 origin = entity.getEyePosition().add(look.scale(0.5));

            HyperBlackHoleMissile missile = new HyperBlackHoleMissile(level, entity, damage);
            missile.setPos(origin);
            missile.shoot(look.x, look.y, look.z, 2.5f, 0.0f);
            level.addFreshEntity(missile);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 0.8f;
    }
}
