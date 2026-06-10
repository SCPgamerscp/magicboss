package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.TridentBombProjectile;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * トライデントボム
 * エンチャントされた魔法のトライデントを投擲し、着弾地点で爆発して
 * 500本のトライデントが全方向に飛び散る。無敵時間無視。
 */
public class TridentBombSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "trident_bomb");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public TridentBombSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower   = 1;
        this.castTime         = 20;   // 1秒
        this.baseManaCost     = 8;
    }


    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.trident_bomb_damage",
                        String.format("%.1f", getSpellDamage(spellLevel)))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        if (!level.isClientSide) {
            float damage = getSpellDamage(spellLevel);
            TridentBombProjectile bomb = new TridentBombProjectile(level, entity, damage);
            bomb.shootFromLook(entity, 2.5f);
            level.addFreshEntity(bomb);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /** スペルパワーベースのダメージ（シャード1本あたり） */
    private float getSpellDamage(int spellLevel) {
        return 8.0f + spellLevel * 4.0f;
    }
}
