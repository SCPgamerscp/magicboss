package com.ecrea.elementmagicboss.enchantment;

import com.ecrea.elementmagicboss.item.MagicGunItem;
import com.ecrea.elementmagicboss.item.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MagicShotgunEnchantment extends Enchantment {
    public MagicShotgunEnchantment() {
        super(Rarity.VERY_RARE, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int pLevel) {
        return 20 + 10 * (pLevel - 1);
    }

    @Override
    public int getMaxCost(int pLevel) {
        return super.getMinCost(pLevel) + 50;
    }

    @Override
    public boolean canEnchant(ItemStack pStack) {
        return pStack.getItem() instanceof BowItem || 
               pStack.getItem() instanceof CrossbowItem || 
               pStack.getItem() instanceof MagicGunItem ||
               super.canEnchant(pStack);
    }


}
