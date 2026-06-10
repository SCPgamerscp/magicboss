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

// Weather cycle: clear -> rain -> thunder -> clear
public class WeatherControlSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "weather_control");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(5)
            .build();

    public WeatherControlSpell() {
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
        return Optional.of(SoundEvents.WEATHER_RAIN);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(world instanceof ServerLevel serverLevel)) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        boolean isRaining  = serverLevel.isRaining();
        boolean isThunder  = serverLevel.isThundering();

        String label;
        if (!isRaining && !isThunder) {
            // Clear -> Rain
            serverLevel.setWeatherParameters(0, 12000, true, false);
            label = "\u96e8"; // 髮ｨ
        } else if (isRaining && !isThunder) {
            // Rain -> Thunder
            serverLevel.setWeatherParameters(0, 12000, true, true);
            label = "\u96f7"; // 髮ｷ
        } else {
            // Thunder -> Clear
            serverLevel.setWeatherParameters(12000, 0, false, false);
            label = "\u6674"; // 譎ｴ
        }

        sendTitle(entity, label);
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private void sendTitle(LivingEntity entity, String text) {
        if (!(entity instanceof ServerPlayer sp)) return;
        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 20));
        sp.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(text).withStyle(net.minecraft.ChatFormatting.YELLOW)));
    }
}