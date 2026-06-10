package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * グーゴル斬撃
 * 斬撃結界の強化版。
 * 半径20ブロック内に 2147483647 ダメージの斬撃を毎tick大量発生させる。
 * 攻撃回数: Lv1=10回/tick, LvN=10*N回/tick (最大Lv10=100回/tick)
 * 60秒間継続。
 * School: Eldritch / CastType: CONTINUOUS / Cooldown: 20s
 */
public class GoogolSlashSpell extends AbstractSpell {

    private static final float RADIUS  = 20f;
    private static final float DAMAGE  = 2147483647f;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "googol_slash");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ELDRITCH_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public GoogolSlashSpell() {
        this.manaCostPerLevel   = 1;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 1;
        this.castTime           = 1200; // 60秒
        this.baseManaCost       = 10;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.googol_slash_damage",
                        String.format("%,d", (long) DAMAGE)),
                Component.translatable("ui.elementmagicboss.googol_slash_hits",
                        getHitsPerTick(spellLevel)),
                Component.translatable("ui.elementmagicboss.slash_barrier_radius", (int) RADIUS)
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.PLAYER_ATTACK_SWEEP);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {

        int hitsPerTick = getHitsPerTick(spellLevel);
        Vec3 center = entity.position();
        AABB area = AABB.ofSize(center, RADIUS * 2, RADIUS * 2, RADIUS * 2);

        List<Entity> targets = level.getEntities(entity, area, t ->
                t instanceof LivingEntity
                && !DamageSources.isFriendlyFireBetween(entity, t)
                && t.distanceTo(entity) <= RADIUS);

        for (Entity target : targets) {
            for (int i = 0; i < hitsPerTick; i++) {
                resetInvulnerable(target);
                if (target instanceof LivingEntity living) {
                    DamageSources.applyDamage(living, DAMAGE,
                            getDamageSource(entity).setIFrames(0));
                } else if (target instanceof EnderDragonPart part) {
                    part.hurt(level.damageSources().playerAttack(
                            entity instanceof net.minecraft.world.entity.player.Player p ? p : null),
                            DAMAGE);
                    part.parentMob.invulnerableTime = 0;
                }
                resetInvulnerable(target);
            }
        }

        // SWEEP_ATTACKパーティクルをランダム位置に大量スポーン
        for (int i = 0; i < 300; i++) {
            double px = center.x + (level.random.nextDouble() * 2 - 1) * RADIUS;
            double py = center.y + (level.random.nextDouble() * 2 - 1) * (RADIUS * 0.5);
            double pz = center.z + (level.random.nextDouble() * 2 - 1) * RADIUS;
            MagicManager.spawnParticles(level, ParticleTypes.SWEEP_ATTACK,
                    px, py, pz, 1, 0, 0, 0, 0, false);
        }

        if (!targets.isEmpty() && playerMagicData != null
                && playerMagicData.getCastDurationRemaining() % 2 == 0) {
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                    1.0f, 0.6f + level.random.nextFloat() * 0.4f);
        }
    }

    private static void resetInvulnerable(Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        } else if (entity instanceof EnderDragonPart part) {
            part.parentMob.invulnerableTime = 0;
        }
    }

    /** Lv1=10回, Lv2=20回, ... Lv10=100回 */
    private static int getHitsPerTick(int spellLevel) {
        return 10 * spellLevel;
    }
}
