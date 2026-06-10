package com.ecrea.elementmagicboss.sound;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<SoundEvent> SKELETON_KING_MUSIC_1 = registerSoundEvent("skeleton_king_music_1");
    public static final RegistryObject<SoundEvent> SKELETON_KING_MUSIC_2 = registerSoundEvent("skeleton_king_music_2");
    public static final RegistryObject<SoundEvent> SKELETON_KING_MUSIC_3 = registerSoundEvent("skeleton_king_music_3");

    // Restoring missing sound events
    public static final RegistryObject<SoundEvent> RAZIEL_BATTLE_MUSIC_1 = registerSoundEvent("raziel_battle_music_1");
    public static final RegistryObject<SoundEvent> RAZIEL_BATTLE_MUSIC_2 = registerSoundEvent("raziel_battle_music_2");
    public static final RegistryObject<SoundEvent> BATTLE_MUSIC_1 = registerSoundEvent("battle_music_1");
    public static final RegistryObject<SoundEvent> BATTLE_MUSIC_2 = registerSoundEvent("battle_music_2");

    // Abyss Blast sounds
    public static final RegistryObject<SoundEvent> ABYSS_BLAST_CHARGE = registerSoundEvent("abyss_blast_only_charge");
    public static final RegistryObject<SoundEvent> ABYSS_BLAST_SHOOT = registerSoundEvent("abyss_blast_only_shoot");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ElementMagicBossMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
