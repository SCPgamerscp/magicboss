package com.ecrea.elementmagicboss.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

// Sure Arrow: applies SureHit effect on arrow hit. Lv1=10s, Lv2=20s ... Lv5=50s
public class SureArrowEnchantment extends Enchantment {

    public SureArrowEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BOW,
                new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override public int getMaxLevel()          { return 5; }
    @Override public int getMinCost(int level)  { return 10 + (level - 1) * 10; }
    @Override public int getMaxCost(int level)  { return getMinCost(level) + 30; }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || super.canEnchant(stack);
    }
}