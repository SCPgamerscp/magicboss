package com.ecrea.elementmagicboss.enchantment;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = 
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<Enchantment> ETERNAL_CURSE = ENCHANTMENTS.register("eternal_curse",
            () -> new EternalCurseEnchantment());

    public static final RegistryObject<Enchantment> ULTIMATE_PROTECTION = ENCHANTMENTS.register("ultimate_protection",
            () -> new CustomProtectionEnchantment(Enchantment.Rarity.VERY_RARE, 0.5f));

    public static final RegistryObject<Enchantment> ADVANCED_PROTECTION = ENCHANTMENTS.register("advanced_protection",
            () -> new CustomProtectionEnchantment(Enchantment.Rarity.RARE, 0.4f));

    public static final RegistryObject<Enchantment> GREATER_PROTECTION = ENCHANTMENTS.register("greater_protection",
            () -> new CustomProtectionEnchantment(Enchantment.Rarity.UNCOMMON, 0.3f));

    public static final RegistryObject<Enchantment> HEALTH_BOOST = ENCHANTMENTS.register("health_boost",
            () -> new GenericStatEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR));

    public static final RegistryObject<Enchantment> REGENERATION_DETERMINATION = ENCHANTMENTS.register("regeneration_determination",
            () -> new RegenerationDeterminationEnchantment());

    public static final RegistryObject<Enchantment> TRUE_MENDING = ENCHANTMENTS.register("true_mending",
            () -> new GenericStatEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.BREAKABLE));

    public static final RegistryObject<Enchantment> SKYROCKET = ENCHANTMENTS.register("skyrocket",
            () -> new GenericStatEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON));

    public static final RegistryObject<Enchantment> MAGIC_AURA = ENCHANTMENTS.register("magic_aura",
            () -> new GenericStatEnchantment(Enchantment.Rarity.VERY_RARE, EnchantmentCategory.ARMOR));

    public static final RegistryObject<Enchantment> MAGIC_WEAKNESS = ENCHANTMENTS.register("magic_weakness",
            () -> new GenericStatEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON));

    public static final RegistryObject<Enchantment> ASSASSIN = ENCHANTMENTS.register("assassin",
            () -> new GenericStatEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.WEAPON));

    public static final RegistryObject<Enchantment> MAGIC_SHOTGUN = ENCHANTMENTS.register("magic_shotgun",
            () -> new MagicShotgunEnchantment());

    public static final RegistryObject<Enchantment> INFINITE_RAPID_FIRE = ENCHANTMENTS.register("infinite_rapid_fire",
            () -> new InfiniteRapidFireEnchantment());    public static final RegistryObject<Enchantment> SURE_ARROW = ENCHANTMENTS.register("sure_arrow",
            () -> new SureArrowEnchantment());

    public static final RegistryObject<Enchantment> NATURE_FORCE = ENCHANTMENTS.register("nature_force",
            () -> new NatureForceEnchantment());


    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
