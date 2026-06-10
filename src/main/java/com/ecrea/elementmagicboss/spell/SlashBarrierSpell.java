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
 * 斬撃結界
 * 飛翔体なし。半径20ブロック内に毎tick20回攻撃する斬撃を発生させる。
 * 無敵時間無視。薙ぎ払いエフェクトが大量に出現する。30秒間継続。
 */
public class SlashBarrierSpell extends AbstractSpell {

    private static final float RADIUS = 20f;
    private static final int   HITS_PER_TICK = 20;


    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "slash_barrier");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public SlashBarrierSpell() {
        this.manaCostPerLevel  = 0;
        this.baseSpellPower    = 5;
        this.spellPowerPerLevel = 1;
        this.castTime          = 600; // 30秒
        this.baseManaCost      = 2;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.slash_barrier_damage",
                        String.format("%.1f", getDamage(spellLevel, caster))),
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

        float damage = getDamage(spellLevel, entity);
        Vec3 center = entity.position();
        AABB area = AABB.ofSize(center, RADIUS * 2, RADIUS * 2, RADIUS * 2);

        // 半径20内の全エンティティを取得
        List<Entity> targets = level.getEntities(entity, area, t ->
                t instanceof LivingEntity
                && !DamageSources.isFriendlyFireBetween(entity, t)
                && t.distanceTo(entity) <= RADIUS);

        // 毎tick20回ダメージ（無敵時間無視）
        for (Entity target : targets) {
            for (int i = 0; i < HITS_PER_TICK; i++) {
                resetInvulnerable(target);
                if (target instanceof LivingEntity living) {
                    DamageSources.applyDamage(living, damage,
                            getDamageSource(entity).setIFrames(0));
                } else if (target instanceof EnderDragonPart part) {
                    part.hurt(level.damageSources().playerAttack(
                            entity instanceof net.minecraft.world.entity.player.Player p ? p : null),
                            damage);
                    part.parentMob.invulnerableTime = 0;
                }
                resetInvulnerable(target);
            }
        }

        // SWEEP_ATTACKパーティクルをランダム位置に大量スポーン（200個/tick）
        for (int i = 0; i < 200; i++) {
            double px = center.x + (level.random.nextDouble() * 2 - 1) * RADIUS;
            double py = center.y + (level.random.nextDouble() * 2 - 1) * (RADIUS * 0.5);
            double pz = center.z + (level.random.nextDouble() * 2 - 1) * RADIUS;
            MagicManager.spawnParticles(level, ParticleTypes.SWEEP_ATTACK,
                    px, py, pz, 1, 0, 0, 0, 0, false);
        }

        // 攻撃対象がいれば薙ぎ払い音を2tickごとに再生
        if (!targets.isEmpty() && playerMagicData != null
                && playerMagicData.getCastDurationRemaining() % 2 == 0) {
            level.playSound(null, center.x, center.y, center.z,
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                    1.0f, 0.8f + level.random.nextFloat() * 0.4f);
        }
    }

    /** invulnerableTimeをhurt()の前後で0にリセット */
    private static void resetInvulnerable(Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        } else if (entity instanceof EnderDragonPart part) {
            part.parentMob.invulnerableTime = 0;
        }
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.0f;
    }
}
