package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// AntiHeal: when entity with AntiHeal effect would heal, cancel it and deal same amount as damage.
@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class AntiHealHandler {

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasEffect(ModMobEffects.ANTI_HEAL.get())) return;

        float heal = event.getAmount();
        event.setCanceled(true);

        // Bypass invulnerability frames and deal damage equal to heal amount
        entity.invulnerableTime = 0;
        entity.hurt(entity.level().damageSources().magic(), heal);
    }
}