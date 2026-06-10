package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.DanmakuShotProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DanmakuShotSpell extends AbstractSpell {
    public static final int PELLET_COUNT = 13;
    public static final float SPREAD_ANGLE_DEGREES = 14.0F;
    public static final float DAMAGE_MULTIPLIER = 0.54F;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_shot");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public DanmakuShotSpell() {
        this.manaCostPerLevel = 1;  // 1/10 (元: 6)
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 5;       // 1/10 (元: 50)
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getPelletDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", PELLET_COUNT)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            RandomSource random = entity.getRandom();
            float damage = getPelletDamage(spellLevel, entity);
            Vec3 spawnPos = entity.position().add(0.0D, entity.getEyeHeight() - 0.15D, 0.0D);
            Vec3 look = entity.getLookAngle().normalize();

            for (int i = 0; i < PELLET_COUNT; i++) {
                DanmakuShotProjectile projectile = new DanmakuShotProjectile(level, entity);
                projectile.setPos(spawnPos);
                projectile.setDamage(damage);
                projectile.setVariant(random.nextInt(PELLET_COUNT));
                projectile.shoot(randomizedDirection(look, random));
                level.addFreshEntity(projectile);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    private float getPelletDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * DAMAGE_MULTIPLIER;
    }

    private static Vec3 randomizedDirection(Vec3 look, RandomSource random) {
        float spread = SPREAD_ANGLE_DEGREES * Mth.DEG_TO_RAD;
        float yawOffset = (random.nextFloat() * 2.0F - 1.0F) * spread;
        float pitchOffset = (random.nextFloat() * 2.0F - 1.0F) * spread;
        return look.yRot(yawOffset).xRot(pitchOffset).normalize();
    }
}