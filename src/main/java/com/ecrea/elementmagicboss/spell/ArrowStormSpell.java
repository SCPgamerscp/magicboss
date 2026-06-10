package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ArrowStormArrow;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * アローストーム
 * TrueBlazingStormのMagicArrow版。
 * 5tickごとに20本の魔法の矢をショットガン状に発射する。無敵時間無視。
 */
public class ArrowStormSpell extends AbstractSpell {

    public static final int ARROW_COUNT    = 20;
    private static final int FIRE_INTERVAL = 5;
    private static final float INACCURACY  = 0.18f;


    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "arrow_storm");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public ArrowStormSpell() {
        this.manaCostPerLevel  = 0;
        this.baseSpellPower    = 8;
        this.spellPowerPerLevel = 1;
        this.castTime          = 60 - 5;
        this.baseManaCost      = 1;
    }

    @Override
    public int getCastTime(int spellLevel) {
        return castTime + 5 * spellLevel;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", ARROW_COUNT)
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ARROW_SHOOT);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }


    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        if (playerMagicData == null) return;
        if ((playerMagicData.getCastDurationRemaining() + 1) % FIRE_INTERVAL != 0) return;
        shootArrowBurst(level, spellLevel, entity);
    }

    private void shootArrowBurst(Level level, int spellLevel, LivingEntity entity) {
        Vec3 look   = entity.getLookAngle();
        Vec3 origin = entity.getEyePosition().add(look.normalize().scale(0.4));
        float damage = getDamage(spellLevel, entity);

        for (int i = 0; i < ARROW_COUNT; i++) {
            ArrowStormArrow arrow = new ArrowStormArrow(level, entity, damage);
            arrow.setPos(origin);
            arrow.shoot(look.x, look.y, look.z, 2.7f, INACCURACY * 20f);
            level.addFreshEntity(arrow);
        }

        level.playSound(null, origin.x, origin.y, origin.z,
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.5f, 0.9f + Utils.random.nextFloat() * 0.2f);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.2f;
    }
}
