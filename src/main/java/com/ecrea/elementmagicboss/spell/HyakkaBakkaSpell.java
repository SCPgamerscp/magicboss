package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 百破爆花 (Hyakka Bakka)
 * 狙った場所の半径10ブロック以内に大量の花火が連続爆発する
 * School: Evocation / CastType: Instant / Cooldown: 1s
 */
public class HyakkaBakkaSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "hyakka_bakka");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public HyakkaBakkaSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(getFireworkDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", HyakkaBakkaHandler.BLAST_RADIUS),
                Component.translatable("ui.elementmagicboss.strikes", HyakkaBakkaHandler.FIREWORK_COUNT)
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
            // 視線の先（最大40ブロック）をターゲット地点として取得
            Vec3 target = Utils.raycastForEntity(serverLevel, entity, 40, true).getLocation();
            HyakkaBakkaHandler.schedule(serverLevel, entity, target,
                    getFireworkDamage(spellLevel, entity), this);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public float getFireworkDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster) * 1.2f;
    }
}
