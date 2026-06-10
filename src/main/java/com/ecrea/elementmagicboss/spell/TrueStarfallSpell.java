package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
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
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.comet.Comet;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.TargetAreaCastData;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TrueStarfallSpell extends AbstractSpell {
    public static final String TRUE_STARFALL_TAG = "elementmagicboss_true_starfall";
    private static final int CAST_TICKS = 480;
    private static final float RADIUS = 30.0f;
    private static final int COMETS_PER_WAVE = 120;

    private final ResourceLocation spellId = new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_starfall");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(16)
            .build();

    public TrueStarfallSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = CAST_TICKS;
        this.baseManaCost = 7;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(CAST_TICKS, 1))
        );
    }

    @Override public CastType getCastType() { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ENDER_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_OVERHEAD;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!(playerMagicData.getAdditionalCastData() instanceof TargetAreaCastData)) {
            Vec3 targetArea = Utils.moveToRelativeGroundLevel(world,
                    Utils.raycastForEntity(world, entity, 60, true).getLocation(), 12);
            playerMagicData.setAdditionalCastData(new TargetAreaCastData(targetArea,
                    TargetedAreaEntity.createTargetAreaEntity(world, targetArea, RADIUS, 0x60008c)));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null || (playerMagicData.getCastDurationRemaining() + 1) % 4 != 0) {
            return;
        }
        if (!(playerMagicData.getAdditionalCastData() instanceof TargetAreaCastData targetAreaCastData)) {
            return;
        }
        Vec3 center = targetAreaCastData.getCenter();
        for (int i = 0; i < COMETS_PER_WAVE; i++) {
            Vec3 spawn = center.add(new Vec3(0.0D, 0.0D, entity.getRandom().nextFloat() * RADIUS)
                    .yRot(entity.getRandom().nextInt(360) * ((float) Math.PI / 180.0F)));
            spawn = raiseWithCollision(spawn, 12, level);
            shootComet(level, spellLevel, entity, spawn);
            MagicManager.spawnParticles(level, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, false);
            MagicManager.spawnParticles(level, ParticleHelper.COMET_FOG, spawn.x, spawn.y, spawn.z, 1, 1, 1, 1, 1, true);
        }
    }

    private Vec3 raiseWithCollision(Vec3 start, int blocks, Level level) {
        for (int i = 0; i < blocks; i++) {
            Vec3 raised = start.add(0, 1, 0);
            if (level.getBlockState(BlockPos.containing(raised)).isAir()) {
                start = raised;
            } else {
                break;
            }
        }
        return start;
    }

    private void shootComet(Level world, int spellLevel, LivingEntity entity, Vec3 spawn) {
        Comet comet = new Comet(world, entity);
        comet.setPos(spawn.add(-1.0D, 0.0D, 0.0D));
        comet.shoot(new Vec3(0.15f, -0.85f, 0.0f), 0.075f);
        comet.setDamage(getDamage(spellLevel, entity));
        comet.setExplosionRadius(2.0f);
        comet.addTag(TRUE_STARFALL_TAG);
        world.addFreshEntity(comet);
        world.playSound(null, spawn.x, spawn.y, spawn.z, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.PLAYERS, 3.0f, 0.7f + Utils.random.nextFloat() * 0.3f);
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f;
    }

    @Override
    public io.redspace.ironsspellbooks.damage.SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
