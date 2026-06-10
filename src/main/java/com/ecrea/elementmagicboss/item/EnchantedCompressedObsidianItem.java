package com.ecrea.elementmagicboss.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


public class EnchantedCompressedObsidianItem extends Item {
    public EnchantedCompressedObsidianItem(Properties pProperties) {
        super(pProperties.food(new FoodProperties.Builder()
                .nutrition(50)
                .saturationMod(50.0f)
                .alwaysEat()
                .build()));
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!pLevel.isClientSide) {
            // Resistance V (amplifier 4) for 10 seconds (200 ticks)
            pLivingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 4));
        }
        return super.finishUsingItem(pStack, pLevel, pLivingEntity);
    }
}
