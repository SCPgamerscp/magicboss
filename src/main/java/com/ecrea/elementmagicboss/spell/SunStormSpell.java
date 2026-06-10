package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

public class SunStormSpell extends AbstractSpell {

    /** 一度に召喚するサンビームの総数 */
    public static final int BEAM_COUNT = 200;

    /** 全ビームを展開するのにかけるtick数 */
    public static final int BURST_DURATION_TICKS = 40;

    /** キャスター中心の展開半径 */
    public static final float STRIKE_RADIUS = 10.0F;

    /** ダメージ乗数 */
    public static final float DAMAGE_MULTIPLIER = 0.60F;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "sun_storm");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(2)
            .build();

    public SunStormSpell() {
        this.manaCostPerLevel = 4;   // 1/4 (元: 15)
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 30;      // 1/4 (元: 120)
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getBeamDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", (int) STRIKE_RADIUS),
                Component.translatable("ui.elementmagicboss.beams", BEAM_COUNT)
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
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (level instanceof ServerLevel serverLevel) {
            SunStormHandler.schedule(serverLevel, entity,
                    entity.position(), getBeamDamage(spellLevel, entity), this);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getBeamDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * DAMAGE_MULTIPLIER;
    }
}
