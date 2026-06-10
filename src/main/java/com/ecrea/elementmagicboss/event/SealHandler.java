package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class SealHandler {

    // Seal: reduce all outgoing damage (physical + magic) by 50%
    // Musou: increase all outgoing damage by 50%
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity le)) return;

        if (le.hasEffect(ModMobEffects.SEAL.get())) {
            event.setAmount(event.getAmount() * 0.5f);
        }
        if (le.hasEffect(ModMobEffects.MUSOU.get())) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }

    // Seal: reduce healing by 50%
    // Musou: increase healing by 50%
    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.hasEffect(ModMobEffects.SEAL.get())) {
            event.setAmount(event.getAmount() * 0.5f);
        }
        if (entity.hasEffect(ModMobEffects.MUSOU.get())) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }
}