package com.ecrea.elementmagicboss.item;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = 
            DeferredRegister.create(ForgeRegistries.ITEMS, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<Item> ENCHANTED_COMPRESSED_OBSIDIAN = ITEMS.register("enchanted_compressed_obsidian",
            () -> new EnchantedCompressedObsidianItem(new Item.Properties().rarity(Rarity.RARE)));

    public static final RegistryObject<Item> STAR_OF_THE_GREAT_WIZARD = ITEMS.register("star_of_the_great_wizard",
            () -> new Item(new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<Item> GREAT_WIZARD_RING = ITEMS.register("great_wizard_ring",
            () -> new GreatWizardRingItem(new Item.Properties()));

    // Angel Raziel Spawn Egg - 天使ラジエルのスポーンエッグ
    public static final RegistryObject<Item> ANGEL_RAZIEL_SPAWN_EGG = ITEMS.register("angel_raziel_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.ANGEL_RAZIEL,
                    0xFFFFFF,  // 背景色（白）
                    0xFFD700,  // 斑点色（金）
                    new Item.Properties()
            ));

    // Ice Sword - 氷の剣
    public static final RegistryObject<Item> ICE_SWORD = ITEMS.register("ice_sword",
            () -> new IceSwordItem(new Item.Properties().rarity(Rarity.EPIC)));

    // Magic Gun - マジックガン
    public static final RegistryObject<Item> MAGIC_GUN = ITEMS.register("magic_gun",
            () -> new MagicGunItem(new Item.Properties()));

    // Fire Sword - 炎の剣
    public static final RegistryObject<Item> FIRE_SWORD = ITEMS.register("fire_sword",
            () -> new FireSwordItem());

    // Skeleton King Spawn Egg - スケルトンキングのスポーンエッグ
    public static final RegistryObject<Item> SKELETON_KING_SPAWN_EGG = ITEMS.register("skeleton_king_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.SKELETON_KING,
                    0x000000,  // 背景色（黒）
                    0xFFFFFF,  // 斑点色（白）
                    new Item.Properties()
            ));

    // Angel Ring - 天使の指輪
    public static final RegistryObject<Item> ANGEL_RING = ITEMS.register("angel_ring",
            () -> new AngelRingItem(new Item.Properties()));

    public static final RegistryObject<Item> POWERFUL_BLESSING_RING = ITEMS.register("powerful_blessing_ring",
            () -> new PowerfulBlessingRingItem(new Item.Properties()));

    // Spell Level Ring - 魔法レベルリング
    public static final RegistryObject<Item> SPELL_LEVEL_RING = ITEMS.register("spell_level_ring",
            () -> new SpellLevelRingItem());

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
