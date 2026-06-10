package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.acid_orb.AcidOrb;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class AcidStormSpell extends AbstractSpell {
    public static final String ACID_STORM_TAG = "elementmagicboss_acid_storm";
    private static final int CAST_TICKS = 200;
    private static final int BURST_INTERVAL = 5;
    private static final int MIN_ORBS_PER_BURST = 12;
    private static final int ORB_VARIANCE = 5;

    private final ResourceLocation spellId = new ResourceLocation(ElementMagicBossMod.MOD_ID, "acid_storm");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public AcidStormSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = CAST_TICKS;
        this.baseManaCost = 9;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(CAST_TICKS, 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", "12-16 / 5 ticks")
        );
    }

    @Override public CastType getCastType() { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.ACID_ORB_CHARGE.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (entity.tickCount % BURST_INTERVAL != 0) {
            return;
        }
        RandomSource random = entity.getRandom();
        Vec3 origin = entity.getEyePosition().add(0.0D, -0.15D, 0.0D);
        int count = MIN_ORBS_PER_BURST + random.nextInt(ORB_VARIANCE);
        for (int i = 0; i < count; i++) {
            Vec3 direction = randomUnitVector(random);
            AcidOrb orb = new AcidOrb(level, entity);
            orb.setPos(origin.add(direction.scale(0.35D)));
            orb.shoot(direction);
            orb.setDeltaMovement(orb.getDeltaMovement().add(0.0D, 0.08D + random.nextDouble() * 0.16D, 0.0D));
            orb.setExplosionRadius(getRadius(spellLevel, entity));
            orb.setRendLevel(getRendAmplifier(spellLevel, entity));
            orb.setRendDuration(getRendDuration(spellLevel, entity));
            orb.addTag(ACID_STORM_TAG);
            level.addFreshEntity(orb);
        }
    }

    private float getRadius(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 3.0f;
    }

    private int getRendAmplifier(int spellLevel, LivingEntity caster) {
        return (int) (getSpellPower(spellLevel, caster) * spellLevel - 1);
    }

    private int getRendDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }

    private static Vec3 randomUnitVector(RandomSource random) {
        double yaw = random.nextDouble() * Math.PI * 2.0D;
        double y = random.nextDouble() * 2.0D - 1.0D;
        double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        return new Vec3(Math.cos(yaw) * horizontal, y, Math.sin(yaw) * horizontal);
    }

    @Override
    public io.redspace.ironsspellbooks.damage.SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
