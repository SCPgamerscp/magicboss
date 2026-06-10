package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.client.renderer.AngelRazielRenderer;
import com.ecrea.elementmagicboss.client.renderer.ShoeEntityRenderer;
import com.ecrea.elementmagicboss.entity.projectile.MassBreakHammerEntity;
import net.minecraft.client.renderer.entity.NoopRenderer;
import com.ecrea.elementmagicboss.client.renderer.YinYangBallRenderer;
import com.ecrea.elementmagicboss.client.renderer.HealingCrystalRenderer;
import com.ecrea.elementmagicboss.client.renderer.SlimeBlockRenderer;
import com.ecrea.elementmagicboss.client.renderer.HeartOrbitRenderer;
import com.ecrea.elementmagicboss.client.renderer.ResourceGlowMarkerRenderer;
import com.ecrea.elementmagicboss.client.renderer.ElementEntityRenderer;
import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent; // Added import

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ELEMENT_ENTITY.get(), ElementEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ANGEL_RAZIEL.get(), AngelRazielRenderer::new);
                event.registerEntityRenderer(ModEntities.SHOE.get(), ShoeEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.RESOURCE_GLOW_MARKER.get(), ResourceGlowMarkerRenderer::new);
        event.registerEntityRenderer(ModEntities.MASS_BREAK_HAMMER.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.HEART_ORBIT.get(), HeartOrbitRenderer::new);
        event.registerEntityRenderer(ModEntities.SLIME_BLOCK_PROJECTILE.get(), SlimeBlockRenderer::new);
        event.registerEntityRenderer(ModEntities.HEALING_CRYSTAL.get(), HealingCrystalRenderer::new);
        event.registerEntityRenderer(ModEntities.YIN_YANG_BALL.get(), YinYangBallRenderer::new);
    }
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // AngelRaziel standard layer registration is removed as it uses GeckoLib
    }
}
