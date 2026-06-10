package com.ecrea.elementmagicboss.client.util;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, value = Dist.CLIENT)
public class BossbarManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, BossbarSprite> CUSTOM_BARS = new HashMap<>();
    
    static {
        LOGGER.info("BossbarManager class loaded!");
    }

    public record BossbarSprite(ResourceLocation emptyLocation, ResourceLocation fullLocation, int width, int height, int buffer, int yBarOffset, int textureWidth, int textureHeight) {
        public BossbarSprite(ResourceLocation combined, int width, int height, int buffer, int yBarOffset, int textureWidth, int textureHeight) {
            this(combined, combined, width, height, buffer, yBarOffset, textureWidth, textureHeight);
        }
    }

    public static void startTracking(UUID uuid, BossbarSprite sprite) {
        CUSTOM_BARS.put(uuid, sprite);
    }

    public static void stopTracking(UUID uuid) {
        CUSTOM_BARS.remove(uuid);
    }

    @SubscribeEvent
    public static void renderCustomBossbar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        UUID bossUUID = event.getBossEvent().getId();
        BossbarSprite customSprite = CUSTOM_BARS.get(bossUUID);
        
        if (customSprite != null) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            
            // Use BossbarStacker for the Y position instead of event.getY()
            int y = BossbarStacker.getAndIncrement(customSprite.height + 25);
            int x = (guiGraphics.guiWidth() - customSprite.width) / 2;
            float progress = event.getBossEvent().getProgress();
            
            RenderSystem.enableBlend();
            
            boolean isCombined = customSprite.emptyLocation().equals(customSprite.fullLocation());

            // Draw empty bar
            int vOffsetEmpty = 0;
            int totalTextureHeight = isCombined ? customSprite.textureHeight * 2 : customSprite.textureHeight;
            guiGraphics.blit(customSprite.emptyLocation(), x, y + 12, customSprite.width, customSprite.height, 0, vOffsetEmpty, customSprite.textureWidth, customSprite.textureHeight, customSprite.textureWidth, totalTextureHeight);
            
            // Draw full bar
            int progressWidth = Mth.lerpInt(progress, 0, customSprite.width - customSprite.buffer * 2) + customSprite.buffer;
            if (progressWidth > 0) {
                int vOffsetFull = isCombined ? customSprite.textureHeight : 0;
                int srcWidth = (int)((float)progressWidth / customSprite.width * customSprite.textureWidth);
                if (srcWidth > 0) {
                    guiGraphics.blit(customSprite.fullLocation(), x, y + 12, progressWidth, customSprite.height, 0, vOffsetFull, srcWidth, customSprite.textureHeight, customSprite.textureWidth, totalTextureHeight);
                }
            }

            RenderSystem.disableBlend();

            // Render text
            Component title = event.getBossEvent().getName();
            int textX = (guiGraphics.guiWidth() - Minecraft.getInstance().font.width(title)) / 2;
            int textY = y + 2; // Position title above the bar space
            
            guiGraphics.drawString(Minecraft.getInstance().font, title, textX, textY, 0xFFFFFF, true);

            event.setCanceled(true);
        }
    }
}
