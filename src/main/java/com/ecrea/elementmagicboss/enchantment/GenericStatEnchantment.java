package com.ecrea.elementmagicboss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class GenericStatEnchantment extends Enchantment {
    public GenericStatEnchantment(Rarity rarity, EnchantmentCategory category) {
        super(rarity, category, EquipmentSlot.values());
    }

    @Override
    public int getMinCost(int level) {
        return 15;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
