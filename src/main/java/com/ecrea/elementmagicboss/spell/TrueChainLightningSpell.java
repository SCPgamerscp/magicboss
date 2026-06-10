package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.spells.lightning.ChainLightningSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * 真チェインライトニング
 * ChainLightningSpell の連鎖回数×10、ダメージ×2
 * School: Lightning / Cooldown: 20s / CastType: INSTANT
 */
public class TrueChainLightningSpell extends ChainLightningSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_chain_lightning");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public TrueChainLightningSpell() {
        this.manaCostPerLevel = 7;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new java.util.ArrayList<>(super.getUniqueInfo(spellLevel, caster));
        return info;
    }

    /** ダメージ×2 */
    @Override
    public float getDamage(int spellLevel, LivingEntity caster) {
        return super.getDamage(spellLevel, caster) * 2.0f;
    }

    /** 連鎖回数×10 */
    @Override
    public int getMaxConnections(int spellLevel, LivingEntity caster) {
        return super.getMaxConnections(spellLevel, caster) * 10;
    }
}