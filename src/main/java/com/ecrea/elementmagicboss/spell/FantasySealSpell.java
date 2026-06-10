package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.projectile.YinYangBallEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

// Fantasy Seal: fires 7 yin-yang balls. First hit applies Seal debuff.
// Caster receives Musou buff on cast.
public class FantasySealSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "fantasy_seal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(30)
            .build();

    public FantasySealSpell() {
        this.manaCostPerLevel   = 10;
        this.baseSpellPower     = 20;
        this.spellPowerPerLevel = 5;
        this.castTime           = 20;
        this.baseManaCost       = 100;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(600, 1))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel,
                                          LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 48, 0.35f);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ENDER_EYE_LAUNCH);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (world instanceof ServerLevel serverLevel
                && playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            LivingEntity target = targetData.getTarget(serverLevel);
            if (target == null) {
                super.onCast(world, spellLevel, entity, castSource, playerMagicData);
                return;
            }

            // Caster gets Musou buff when the targeted seal is released.
            entity.addEffect(new MobEffectInstance(
                    ModMobEffects.MUSOU.get(), 600, 0, false, true, true));

            float damage = getSpellPower(spellLevel, entity);
            Vec3 start = entity.getEyePosition().add(entity.getLookAngle().scale(0.6));
            Vec3 aim = target.getBoundingBox().getCenter().subtract(start).normalize();
            Vec3 right = aim.cross(new Vec3(0, 1, 0));
            if (right.lengthSqr() < 0.001) {
                right = entity.getLookAngle().cross(new Vec3(0, 1, 0));
            }
            right = right.normalize();
            Vec3 up = right.cross(aim).normalize();

            for (int i = 0; i < 7; i++) {
                double angle = (Math.PI * 2.0 * i) / 7.0;
                Vec3 spread = right.scale(Math.cos(angle) * 0.18).add(up.scale(Math.sin(angle) * 0.18));
                Vec3 dir = aim.add(spread).normalize();

                YinYangBallEntity ball = new YinYangBallEntity(world, entity, damage);
                ball.setPos(start);
                ball.setHomingTarget(target);
                ball.shoot(dir, 2.0f);
                serverLevel.addFreshEntity(ball);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
