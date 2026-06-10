package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.projectile.ApostleBeamCrossEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * 使徒光線 (Apostle Beam)
 * 狙った場所やエンティティに大爆発を起こし、光の十字架が現れる。
 * School: Holy / Cast Type: Instant / Cooldown: 15s
 */
public class ApostleBeamSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "apostle_beam");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public ApostleBeamSpell() {
        this.manaCostPerLevel   = 8;
        this.baseSpellPower     = 15;
        this.spellPowerPerLevel = 3;
        this.castTime           = 0;   // Instant
        this.baseManaCost       = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.elementmagicboss.apostle_beam_radius",
                        (int) ApostleBeamCrossEntity.DAMAGE_RADIUS)
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.BEACON_ACTIVATE);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 狙った場所を取得 (最大80ブロック先)
            Vec3 targetLocation = Utils.raycastForEntity(level, entity, 80, true).getLocation();

            float damage = getDamage(spellLevel, entity);
            float radius = ApostleBeamCrossEntity.DAMAGE_RADIUS;

            // ── 1. 光の十字架を先に召喚（projectile参照に使う） ──
            ApostleBeamCrossEntity cross = new ApostleBeamCrossEntity(
                    ModEntities.APOSTLE_BEAM_CROSS.get(), serverLevel);
            cross.setOwner(entity);
            cross.setSpellDamage(damage);
            cross.moveTo(targetLocation.x, targetLocation.y, targetLocation.z);
            level.addFreshEntity(cross);

            // ── 2. 大爆発ダメージ ──
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                    net.minecraft.world.phys.AABB.ofSize(targetLocation, radius * 2, radius * 2, radius * 2),
                    living -> living.isAlive()
                            && living != entity
                            && living.distanceToSqr(targetLocation) <= radius * radius
                            && !io.redspace.ironsspellbooks.damage.DamageSources.isFriendlyFireBetween(entity, living));

            for (LivingEntity target : targets) {
                target.invulnerableTime = 0;
                io.redspace.ironsspellbooks.damage.DamageSources.applyDamage(
                        target,
                        damage,
                        getDamageSource(cross, entity)
                );
                target.invulnerableTime = 0;
            }

            // ── 3. 爆発エフェクト ──
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    targetLocation.x, targetLocation.y, targetLocation.z,
                    5, 2.0, 2.0, 2.0, 0.0);
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    targetLocation.x, targetLocation.y + 1.0, targetLocation.z,
                    3, 0.5, 0.5, 0.5, 0.0);
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    targetLocation.x, targetLocation.y + 2.0, targetLocation.z,
                    80, 5.0, 5.0, 5.0, 0.1);
            serverLevel.sendParticles(ParticleTypes.GLOW,
                    targetLocation.x, targetLocation.y + 1.0, targetLocation.z,
                    50, 3.0, 3.0, 3.0, 0.05);

            // 爆発音
            level.playSound(null, targetLocation.x, targetLocation.y, targetLocation.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 4.0f, 0.7f);
            level.playSound(null, targetLocation.x, targetLocation.y, targetLocation.z,
                    SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 3.0f, 1.2f);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** 爆発・十字架共通ダメージ */
    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f;
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
