package com.ecrea.elementmagicboss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class EternalCurseEnchantment extends Enchantment {
    public EternalCurseEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int pLevel) {
        return 20;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
