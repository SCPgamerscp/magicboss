package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

// Sun Reversal: toggles between day (1000) and night (13000)
public class SunReversalSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "sun_reversal");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(5)
            .build();

    public SunReversalSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower   = 1;
        this.spellPowerPerLevel = 0;
        this.castTime         = 60;
        this.baseManaCost     = 30;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of();
    }

    @Override public CastType getCastType()              { return CastType.LONG; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.BEACON_POWER_SELECT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(world instanceof ServerLevel serverLevel)) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        long currentTime = serverLevel.getDayTime() % 24000;
        long dayBase     = (serverLevel.getDayTime() / 24000) * 24000;

        String label;
        if (currentTime < 12000) {
            // Day -> Night
            serverLevel.setDayTime(dayBase + 13000);
            label = "\u591c"; // 夜
        } else {
            // Night -> Morning
            serverLevel.setDayTime(dayBase + 1000);
            label = "\u671d"; // 朝
        }

        sendTitle(entity, label);
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private void sendTitle(LivingEntity entity, String text) {
        if (!(entity instanceof ServerPlayer sp)) return;
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 20));
        sp.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(text).withStyle(net.minecraft.ChatFormatting.AQUA)));
    }
}