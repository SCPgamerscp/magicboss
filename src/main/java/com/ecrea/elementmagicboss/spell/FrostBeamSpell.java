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
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * フロストビーム
 * Ray Of Frostのビームを使い、当たると10秒凍結するビームを照射し続ける。
 * 無敵時間無視で1tickあたり10回ダメージ。20秒間継続。
 */
public class FrostBeamSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "frost_beam");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public FrostBeamSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower   = 5;
        this.spellPowerPerLevel = 1;
        this.castTime         = 400; // 20秒
        this.baseManaCost     = 2;
    }


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.frost_beam_damage",
                        String.format("%.1f", getDamage(spellLevel, caster))),
                Component.translatable("ui.elementmagicboss.frost_beam_freeze",
                        getFreezeSeconds())
        );
    }

    @Override public CastType getCastType()              { return CastType.CONTINUOUS; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.RAY_OF_FROST.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST;
    }

    // ── 毎tick処理 ──────────────────────────────────────────────────────────
    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity,
                                 @Nullable MagicData playerMagicData) {
        float range = getRange();

        // レイキャスト
        HitResult hitResult = Utils.raycastForEntity(level, entity, range, true, 0.15f);

        // Cone of Coldの音をループ再生（8tickごと）
        if (playerMagicData != null && playerMagicData.getCastDurationRemaining() % 8 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundRegistry.CONE_OF_COLD_LOOP.get(), SoundSource.PLAYERS,
                    1.5f, 0.9f + level.random.nextFloat() * 0.2f);
        }

        // ビームエフェクト（RayOfFrostVisualEntityを毎tick生成）
        level.addFreshEntity(new RayOfFrostVisualEntity(
                level, entity.getEyePosition(), hitResult.getLocation(), entity));

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            float damage = getDamage(spellLevel, entity);
            int freezeTicks = getFreezeTicksAmount(target);

            // 1tickあたり10回ヒット（無敵時間をリセットして連続ダメージ）
            for (int i = 0; i < 10; i++) {
                if (target instanceof LivingEntity living) {
                    living.invulnerableTime = 0;
                }
                DamageSources.applyDamage(target, damage,
                        getDamageSource(entity).setFreezeTicks(freezeTicks).setIFrames(0));
            }

            // 凍結を確実に付与（ticksFrozenをtickRequired以上に設定）
            if (target instanceof LivingEntity living) {
                int required = living.getTicksRequiredToFreeze();
                if (living.getTicksFrozen() < required + getFreezeSeconds() * 20) {
                    living.setTicksFrozen(required + getFreezeSeconds() * 20);
                }
            }

            // パーティクル
            MagicManager.spawnParticles(level, ParticleHelper.ICY_FOG,
                    hitResult.getLocation().x, target.getY(), hitResult.getLocation().z,
                    4, 0, 0, 0, .3, true);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE,
                    hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z,
                    5, 0, 0, 0, .3, false);
        }
    }


    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.5f;
    }

    private static float getRange() {
        return 30f;
    }

    /** 凍結秒数 */
    private static int getFreezeSeconds() {
        return 10;
    }

    /** DamageSourceに渡すfreezeTicks量（既存のfreezeTicks + 10秒分） */
    private static int getFreezeTicksAmount(Entity target) {
        int required = (target instanceof LivingEntity le)
                ? le.getTicksRequiredToFreeze() : 140;
        return required + getFreezeSeconds() * 20;
    }
}
