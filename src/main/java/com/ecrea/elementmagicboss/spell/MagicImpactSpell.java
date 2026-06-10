package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.projectile.MagicImpactCrossEntity;
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
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class MagicImpactSpell extends AbstractSpell {
    public static final int DURATION_TICKS = 20 * 120;
    public static final int DAMAGE_INTERVAL_TICKS = 5;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "magic_impact");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public MagicImpactSpell() {
        this.manaCostPerLevel = 12;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 3;
        this.castTime = 20;
        this.baseManaCost = 120;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getTickDamage(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.magic_impact_radius",
                        (int) MagicImpactCrossEntity.DAMAGE_RADIUS),
                Component.translatable("ui.elementmagicboss.magic_impact_duration",
                        Utils.timeFromTicks(DURATION_TICKS, 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            Vec3 targetLocation = Utils.raycastForEntity(level, entity, 100, true).getLocation();
            Vec3 grounded = Utils.moveToRelativeGroundLevel(level, targetLocation, 12);
            Vec3 raycastTarget = grounded.add(0, 20, 0);
            Vec3 spawnLocation = Utils.raycastForBlock(level, grounded, raycastTarget, ClipContext.Fluid.NONE).getLocation();

            MagicImpactCrossEntity cross = new MagicImpactCrossEntity(ModEntities.MAGIC_IMPACT_CROSS.get(), serverLevel);
            cross.setOwner(entity);
            cross.setSpellDamage(getTickDamage(spellLevel, entity));
            cross.moveTo(spawnLocation.x, spawnLocation.y, spawnLocation.z);
            level.addFreshEntity(cross);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getTickDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 2.0f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
