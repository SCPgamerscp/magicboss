package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

// BreedingBlessing: forces all breedable animals within 20 blocks to breed,
// become adults, and get tamed.
public class BreedingBlessingSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "breeding_blessing");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(5)
            .build();

    public BreedingBlessingSpell() {
        this.manaCostPerLevel   = 0;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 0;
        this.castTime           = 0;
        this.baseManaCost       = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of();
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.AMETHYST_CLUSTER_HIT);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(world instanceof ServerLevel serverLevel)) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        Player player = entity instanceof Player p ? p : null;

        AABB area = entity.getBoundingBox().inflate(20);
        List<Animal> animals = serverLevel.getEntitiesOfClass(Animal.class, area,
                Animal::isAlive);

        for (Animal animal : animals) {
            // Reset breeding cooldown and grow up babies (age=0: adult, no cooldown)
            animal.setAge(0);

            // Set all animals in love so they can breed immediately
            animal.setInLove(player instanceof ServerPlayer sp ? sp : null);

            // Tame tameable animals
            if (animal instanceof TamableAnimal tamable && !tamable.isTame()
                    && player instanceof ServerPlayer sp) {
                tamable.tame(sp);
            }
        }

        // Also grow up any AgeableMob babies (villager children, etc.) in range
        serverLevel.getEntitiesOfClass(AgeableMob.class, area,
                m -> m.isAlive() && m.isBaby() && !(m instanceof Animal))
                .forEach(m -> m.setAge(0));

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}