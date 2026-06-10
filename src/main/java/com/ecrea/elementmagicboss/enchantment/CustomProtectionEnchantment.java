package com.ecrea.elementmagicboss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class CustomProtectionEnchantment extends Enchantment {
    private final float reduction;

    public CustomProtectionEnchantment(Rarity rarity, float reduction) {
        super(rarity, EnchantmentCategory.ARMOR, new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
        this.reduction = reduction;
    }

    public float getReduction() {
        return reduction;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
