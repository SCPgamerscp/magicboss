package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

// AntiHeal: applies AntiHeal debuff to the closest entity in line of sight (range 20).
public class AntiHealSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "anti_heal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public AntiHealSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 5;
        this.castTime = 0;
        this.baseManaCost = 40;
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
        return Optional.of(SoundEvents.ARROW_HIT_PLAYER);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        LivingEntity target = findTarget(world, entity);
        if (target != null) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.ANTI_HEAL.get(),
                    getDuration(spellLevel, entity),
                    0, false, true, true));
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private LivingEntity findTarget(Level world, LivingEntity caster) {
        Vec3 start = caster.getEyePosition();
        Vec3 look  = caster.getLookAngle();
        Vec3 end   = start.add(look.scale(20.0));

        AABB searchBox = new AABB(start, end).inflate(1.5);
        List<LivingEntity> candidates = world.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != caster && e.isAlive() && !caster.isAlliedTo(e));

        LivingEntity best = null;
        double bestDist   = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            // Simple ray-AABB check
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(0.3).clip(start, end);
            if (hit.isPresent()) {
                double d = hit.get().distanceToSqr(start);
                if (d < bestDist) { bestDist = d; best = candidate; }
            }
        }
        return best;
    }

    public int getDuration(int spellLevel, LivingEntity caster) {
        return ModSpellDurations.scalingDurationTicks(spellLevel, getSpellPower(spellLevel, caster));
    }
}
