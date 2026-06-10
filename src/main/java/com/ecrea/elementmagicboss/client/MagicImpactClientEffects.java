package com.ecrea.elementmagicboss.client;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.MagicImpactCrossEntity;
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
public final class MagicImpactClientEffects {
    private MagicImpactClientEffects() {
    }

    @SubscribeEvent
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        float intensity = getCurrentIntensity();
        if (intensity <= 0.0f) {
            return;
        }

        event.setRed(Mth.lerp(intensity, event.getRed(), 0.60f));
        event.setGreen(Mth.lerp(intensity, event.getGreen(), 0.06f));
        event.setBlue(Mth.lerp(intensity, event.getBlue(), 0.08f));
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        float intensity = getCurrentIntensity();
        if (intensity <= 0.0f) {
            return;
        }

        event.setNearPlaneDistance(Math.max(2.0f, event.getNearPlaneDistance() * 0.7f));
        event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 48.0f));
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
        if (player == null || level == null || player.tickCount % 3 != 0) {
            return;
        }

        float intensity = getCurrentIntensity();
        if (intensity <= 0.0f) {
            return;
        }

        for (int i = 0; i < 4; i++) {
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 24.0;
            double y = player.getY() + level.random.nextDouble() * 8.0;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 24.0;
            level.addParticle(ParticleTypes.CRIMSON_SPORE, x, y, z, 0.0, 0.002, 0.0);
            level.addParticle(ParticleTypes.ASH, x, y, z, 0.0, -0.003, 0.0);
        }
    }

    private static float getCurrentIntensity() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return 0.0f;
        }

        return level.getEntitiesOfClass(
                MagicImpactCrossEntity.class,
                player.getBoundingBox().inflate(2048.0))
                .isEmpty() ? 0.0f : 1.0f;
    }
}
