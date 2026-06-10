package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.SlimeBlockProjectile;
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

public class SlimeBlockSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "slime_block_spell");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public SlimeBlockSpell() {
        this.manaCostPerLevel   = 5;
        this.baseSpellPower     = 10;
        this.spellPowerPerLevel = 2;
        this.castTime           = 20;
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        String.format("%.1f", getSpellPower(spellLevel, caster) * 2.0f))
        );
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.SLIME_BLOCK_PLACE);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (world instanceof ServerLevel) {
            float damage = getSpellPower(spellLevel, entity) * 2.0f;
            float speed  = 1.5f + spellLevel * 0.1f;
            SlimeBlockProjectile proj = new SlimeBlockProjectile(world, entity, damage);
            proj.setPos(entity.getEyePosition().add(entity.getLookAngle().scale(0.15)).subtract(0, 0.15, 0));
            proj.shoot(entity.getLookAngle(), speed);
            world.addFreshEntity(proj);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
