package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.AngelRazielEntity;
import com.ecrea.elementmagicboss.entity.ElementEntity;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.entity.SkeletonKingEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ELEMENT_ENTITY.get(), ElementEntity.createAttributes().build());
        event.put(ModEntities.ANGEL_RAZIEL.get(), AngelRazielEntity.createAttributes().build());
        event.put(ModEntities.SKELETON_KING.get(), SkeletonKingEntity.createAttributes().build());
    }
}
