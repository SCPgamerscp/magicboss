package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.accessor.ChainChainAccessor;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.spells.evocation.ChainCreeperSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * チェインチェインクリーパー
 * Chain Creeperの応用。対象の周囲にクリーパーの頭部のリングを召喚し、
 * 着弾で爆発。爆発地点から新たなリングが生まれ、3回繰り返す。
 * School: Evocation / Cast Type: Long / Cooldown: 15s
 */
public class ChainChainCreeperSpell extends AbstractSpell {
    public static final int MAX_CHAINS = 3;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "chain_chain_creeper");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(15)
            .build();

    public ChainChainCreeperSpell() {
        this.manaCostPerLevel   = 10;
        this.baseSpellPower     = 5;
        this.spellPowerPerLevel = 0;
        this.castTime           = 30;
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getSpellPower(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count",
                        getCount(spellLevel, caster)),
                Component.translatable("ui.elementmagicboss.chain_chain_creeper_chains",
                        MAX_CHAINS)
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.CREEPER_PRIMED);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        Utils.preCastTargetHelper(level, entity, playerMagicData, this, 48, .25f, false);
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        Vec3 spawn = null;
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData castTargetingData) {
            spawn = castTargetingData.getTargetPosition((ServerLevel) level);
        }
        if (spawn == null) {
            HitResult raycast = Utils.raycastForEntity(level, entity, 32, true);
            if (raycast.getType() == HitResult.Type.ENTITY) {
                spawn = ((EntityHitResult) raycast).getEntity().position();
            } else {
                spawn = Utils.moveToRelativeGroundLevel(level, raycast.getLocation()
                        .subtract(entity.getForward().normalize()).add(0, 2, 0), 5);
            }
        }

        int count = getCount(spellLevel, entity);
        float damage = getDamage(spellLevel, entity);
        Vec3 origin = spawn.add(0, 0.5, 0);

        // ISBのsummonCreeperRingでリングを生成
        ChainCreeperSpell.summonCreeperRing(level, entity, origin, damage, count);

        // 生成直後のCreeperHeadProjectileにチェインチェインフラグを設定
        level.getEntitiesOfClass(CreeperHeadProjectile.class,
                new AABB(origin, origin).inflate(5.0)).forEach(head -> {
            if (head.tickCount <= 1 && head instanceof ChainChainAccessor accessor) {
                accessor.elementmagicboss$setChainChainData(MAX_CHAINS - 1, count, damage);
            }
        });

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getCount(int spellLevel, LivingEntity entity) {
        return 3 + spellLevel - 1;
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return this.getSpellPower(spellLevel, entity);
    }
}
