package com.ecrea.elementmagicboss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class InfiniteRapidFireEnchantment extends Enchantment {
    public InfiniteRapidFireEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int pLevel) {
        return 30;
    }

    @Override
    public int getMaxCost(int pLevel) {
        return 65;
    }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem() instanceof BowItem ||
               pStack.getItem() instanceof CrossbowItem ||
               super.canEnchant(pStack);
    }
}
