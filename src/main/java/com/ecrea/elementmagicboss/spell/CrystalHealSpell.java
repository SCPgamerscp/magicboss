package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.HealingCrystalEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class CrystalHealSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "crystal_heal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public CrystalHealSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 10;
        this.spellPowerPerLevel = 2;
        this.castTime           = 20;
        this.baseManaCost       = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.heal",
                        String.format("%.1f", getHealAmount(spellLevel, caster)))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.AMETHYST_BLOCK_RESONATE);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (world instanceof ServerLevel serverLevel) {
            float heal = getHealAmount(spellLevel, entity);
            HealingCrystalEntity crystal = new HealingCrystalEntity(world, entity, heal);
            // Spawn slightly in front of caster
            crystal.setPos(entity.getX(), entity.getY(), entity.getZ());
            serverLevel.addFreshEntity(crystal);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getHealAmount(int spellLevel, LivingEntity caster) {
        // Lv1=1.0, Lv5=3.0, Lv10=5.5
        return 0.5f + spellLevel * 0.5f;
    }
}