package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class RareDropGuaranteeHandler {

    @SubscribeEvent
    public static void onLootingLevel(LootingLevelEvent event) {
        Entity attacker = event.getDamageSource().getEntity();
        if (!(attacker instanceof LivingEntity le)) return;
        if (!le.hasEffect(ModMobEffects.RARE_DROP_GUARANTEE.get())) return;
        event.setLootingLevel(event.getLootingLevel() + 100);
    }
}