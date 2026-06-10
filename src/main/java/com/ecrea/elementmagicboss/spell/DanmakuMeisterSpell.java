package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.DanmakuMeisterSweepEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class DanmakuMeisterSpell extends AbstractSpell {
    public static final String DANMAKU_MEISTER_PROJECTILE_TAG = "elementmagicboss_danmaku_meister_projectile";
    public static final String DANMAKU_MEISTER_LASER_TAG = "elementmagicboss_danmaku_meister_laser";
    public static final int DURATION_TICKS = 20;
    public static final int SWEEP_COUNT = 15;
    public static final int TOTAL_PROJECTILES = DURATION_TICKS * SWEEP_COUNT * 3;
    public static final float DAMAGE_MULTIPLIER = 0.6f;

    private final ResourceLocation spellId = new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_meister");
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public DanmakuMeisterSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getProjectileDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.projectile_count", TOTAL_PROJECTILES),
                Component.translatable("ui.elementmagicboss.danmaku_meister_lasers", 26)
        );
    }

    @Override public CastType getCastType() { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig() { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            DanmakuMeisterSweepEntity sweep = new DanmakuMeisterSweepEntity(level, entity, getProjectileDamage(spellLevel, entity));
            sweep.setPos(entity.getX(), entity.getEyeY() - 0.15D, entity.getZ());
            level.addFreshEntity(sweep);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getProjectileDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * DAMAGE_MULTIPLIER;
    }

    @Override
    public io.redspace.ironsspellbooks.damage.SpellDamageSource getDamageSource(Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }
}
