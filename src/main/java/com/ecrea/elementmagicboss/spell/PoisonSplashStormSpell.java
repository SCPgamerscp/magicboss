package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.PoisonSplashStormFieldEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class PoisonSplashStormSpell extends AbstractSpell {
    public static final String POISON_SPLASH_STORM_TAG = "elementmagicboss_poison_splash_storm";
    private static final int DURATION_TICKS = 200;
    private static final float RADIUS = 20.0f;

    private final ResourceLocation spellId = new ResourceLocation(ElementMagicBossMod.MOD_ID, "poison_splash_storm");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public PoisonSplashStormSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 55;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(DURATION_TICKS, 1))
        );
    }

    @Override public CastType getCastType() { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.POISON_SPLASH_BEGIN.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.POISON_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_LONG_CAST;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 40, 0.35f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 spawn = resolveTarget(level, entity, playerMagicData);
            PoisonSplashStormFieldEntity field = new PoisonSplashStormFieldEntity(level, entity,
                    getDamage(spellLevel, entity), getDuration(spellLevel, entity));
            field.moveTo(spawn.x, spawn.y, spawn.z, entity.getYRot(), 0.0F);
            level.addFreshEntity(field);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private Vec3 resolveTarget(Level level, LivingEntity entity, MagicData playerMagicData) {
        Vec3 spawn = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            spawn = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (spawn == null) {
            HitResult raycast = Utils.raycastForEntity(level, entity, 40, true);
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level,
                        raycast.getLocation().subtract(entity.getForward().normalize()).add(0, 2, 0), 5);
            }
        }
        return spawn;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
    }

    private int getDuration(int spellLevel, LivingEntity entity) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, entity));
    }

    @Override
    public io.redspace.ironsspellbooks.damage.SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
