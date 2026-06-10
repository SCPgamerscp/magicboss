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
import io.redspace.ironsspellbooks.entity.spells.blood_slash.BloodSlashProjectile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlyingBloodSlashSpell extends AbstractSpell {
    /** 持続時間: 15秒 = 300tick */
    private static final int CAST_TICKS = 300;

    /** 1秒あたり500本 → 毎tick 500/20 = 25本 */
    private static final int SLASHES_PER_TICK = 25;

    /** 合計本数: 300tick × 25本 = 7500本 */
    public static final int TOTAL_SLASHES = CAST_TICKS * SLASHES_PER_TICK;

    private static final float DAMAGE_MULTIPLIER = 1.0f; // 2倍 (元: 0.5f)

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "flying_blood_slash");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public FlyingBloodSlashSpell() {
        this.manaCostPerLevel = 1;   // 元の1/5
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 1;
        this.castTime = CAST_TICKS;
        this.baseManaCost = 8;       // 元の1/5
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getSlashDamage(spellLevel, caster), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", TOTAL_SLASHES)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
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
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity entity, @Nullable MagicData playerMagicData) {
        if (playerMagicData == null) {
            return;
        }

        Vec3 origin = entity.getEyePosition().add(entity.getLookAngle().normalize().scale(0.4f));
        float damage = getSlashDamage(spellLevel, entity);
        RandomSource random = entity.getRandom();

        for (int i = 0; i < SLASHES_PER_TICK; i++) {
            Vec3 direction = randomUnitVector(random);
            BloodSlashProjectile slash = new BloodSlashProjectile(level, entity);
            slash.setPos(origin);
            slash.shoot(direction);
            slash.setDamage(damage);
            level.addFreshEntity(slash);
        }
    }

    private float getSlashDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * DAMAGE_MULTIPLIER;
    }

    private static Vec3 randomUnitVector(RandomSource random) {
        double phi = random.nextDouble() * (Math.PI * 2.0);
        double cosTheta = random.nextDouble() * 2.0 - 1.0;
        double sinTheta = Math.sqrt(1.0 - cosTheta * cosTheta);
        return new Vec3(Math.cos(phi) * sinTheta, cosTheta, Math.sin(phi) * sinTheta);
    }
}
