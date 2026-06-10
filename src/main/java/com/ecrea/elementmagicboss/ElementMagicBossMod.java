package com.ecrea.elementmagicboss;

import com.ecrea.elementmagicboss.item.ModItems;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.spell.ModSpells;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ElementMagicBossMod.MOD_ID)
public class ElementMagicBossMod {
    public static final String MOD_ID = "elementmagicboss";

    public ElementMagicBossMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModMobEffects.register(modEventBus);
        com.ecrea.elementmagicboss.item.ModCreativeModeTabs.register(modEventBus);
        com.ecrea.elementmagicboss.entity.ModEntities.register(modEventBus);
        com.ecrea.elementmagicboss.sound.ModSounds.register(modEventBus);
        com.ecrea.elementmagicboss.enchantment.ModEnchantments.register(modEventBus);
        ModSpells.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        System.out.println("ElementMagicBossMod: Initializing and registering events...");
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(com.ecrea.elementmagicboss.event.ModEvents.class);
        MinecraftForge.EVENT_BUS.register(com.ecrea.elementmagicboss.event.InfiniteRapidFireHandler.class);
        MinecraftForge.EVENT_BUS.register(com.ecrea.elementmagicboss.event.SureHitProjectileHandler.class);
    }

}
