package com.ecrea.elementmagicboss.client;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.TimeStopFieldEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, value = Dist.CLIENT)
public final class TheWorldClientEffects {
    private TheWorldClientEffects() {
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float intensity = getCurrentIntensity();
        if (intensity <= 0.0f) {
            return;
        }

        event.setRed(Mth.lerp(intensity, event.getRed(), 0.40f));
        event.setGreen(Mth.lerp(intensity, event.getGreen(), 0.42f));
        event.setBlue(Mth.lerp(intensity, event.getBlue(), 0.52f));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float intensity = getCurrentIntensity();
        if (intensity <= 0.0f) {
            return;
        }

        event.setNearPlaneDistance(Math.max(1.0f, event.getNearPlaneDistance() * 0.8f));
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 72.0f));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || player.tickCount % 4 != 0) {
            return;
        }

        if (getCurrentIntensity() <= 0.0f) {
            return;
        }

        for (int i = 0; i < 3; i++) {
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 14.0;
            double y = player.getY() + level.random.nextDouble() * 3.0;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 14.0;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    private static float getCurrentIntensity() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return 0.0f;
        }

        return level.getEntitiesOfClass(TimeStopFieldEntity.class, player.getBoundingBox().inflate(4096.0D))
                .isEmpty() ? 0.0f : 1.0f;
    }
}
