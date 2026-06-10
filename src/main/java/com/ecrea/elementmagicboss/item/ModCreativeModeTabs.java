package com.ecrea.elementmagicboss.item;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAGIC_BOSS_TAB = CREATIVE_MODE_TABS.register("magic_boss_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.STAR_OF_THE_GREAT_WIZARD.get()))
                    .title(Component.translatable("creativetab.magic_boss_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.ENCHANTED_COMPRESSED_OBSIDIAN.get());
                        pOutput.accept(ModItems.STAR_OF_THE_GREAT_WIZARD.get());
                        pOutput.accept(ModItems.GREAT_WIZARD_RING.get());
                        pOutput.accept(ModItems.ICE_SWORD.get());
                        pOutput.accept(ModItems.FIRE_SWORD.get());
                        pOutput.accept(ModItems.MAGIC_GUN.get());
                        // Angel Rasielのスポーンエッグを追加
                        pOutput.accept(ModItems.ANGEL_RAZIEL_SPAWN_EGG.get());
                        pOutput.accept(ModItems.SKELETON_KING_SPAWN_EGG.get());
                        pOutput.accept(ModItems.ANGEL_RING.get());
                        pOutput.accept(ModItems.POWERFUL_BLESSING_RING.get());
                        pOutput.accept(ModItems.SPELL_LEVEL_RING.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
