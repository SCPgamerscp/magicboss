package com.ecrea.elementmagicboss.client.util;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.ElementEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, value = Dist.CLIENT)
public class ElementBossbarRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation EMPTY_TEXTURE = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_empty.png");
    private static final ResourceLocation FULL_TEXTURE = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_full.png");
    private static final int MAX_BOSSBARS = 3; // 譛螟ｧ3縺､縺ｾ縺ｧ陦ｨ遉ｺ
    private static Field eventsField = null;
    
    static {
        try {
            // BossHealthOverlay縺ｮevents繝輔ぅ繝ｼ繝ｫ繝峨↓繧｢繧ｯ繧ｻ繧ｹ縺吶ｋ縺溘ａ縺ｮ繝ｪ繝輔Ξ繧ｯ繧ｷ繝ｧ繝ｳ
            eventsField = net.minecraft.client.gui.components.BossHealthOverlay.class.getDeclaredField("events");
            eventsField.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to get events field from BossHealthOverlay", e);
        }
    }
    
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        
        // Calculate initial offset from vanilla bossbars
        int vanillaBossbarCount = 0;
        try {
            if (eventsField != null) {
                Map<?, ?> events = (Map<?, ?>) eventsField.get(mc.gui.getBossOverlay());
                vanillaBossbarCount = events.size();
            }
        } catch (Exception e) {
            // Ignored, defaults to 0
        }
        
        // Reset the shared stacker for this frame
        BossbarStacker.reset(vanillaBossbarCount * 19);
        
        // Find and sort ElementEntities
        // Use a defensive copy to avoid ConcurrentModificationException
        List<ElementEntity> elementEntities = new ArrayList<>();
        try {
            List<LivingEntity> nearby = new ArrayList<>(
                mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(64.0D)));
            for (LivingEntity entity : nearby) {
                if (entity instanceof ElementEntity && !entity.isRemoved()) {
                    elementEntities.add((ElementEntity) entity);
                }
            }
        } catch (Exception ignored) {
            return;
        }
        elementEntities.sort(Comparator.comparingDouble(e -> mc.player.distanceToSqr(e)));
        
        // Render up to MAX_BOSSBARS
        int count = Math.min(elementEntities.size(), MAX_BOSSBARS);
        for (int i = 0; i < count; i++) {
            renderElementBossbar(event.getGuiGraphics(), mc, elementEntities.get(i));
        }
    }
    
    private static void renderElementBossbar(GuiGraphics guiGraphics, Minecraft mc, ElementEntity entity) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int barWidth = 256;
        int barHeight = 100;
        int x = (screenWidth - barWidth) / 2;
        
        // Use the shared BossbarStacker for consistent Y positioning
        // 110 = bar height (100) + safe margin (10)
        int y = BossbarStacker.getAndIncrement(110) + 2;
        
        float healthPercentage = entity.getHealth() / entity.getMaxHealth();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // Draw empty bar
        guiGraphics.blit(EMPTY_TEXTURE, x, y, 0, 0, barWidth, barHeight, 256, 100);
        
        // Draw filled bar
        int filledWidth = (int) (barWidth * healthPercentage);
        if (filledWidth > 0) {
            guiGraphics.blit(FULL_TEXTURE, x, y, 0, 0, filledWidth, barHeight, 256, 100);
        }
        
        RenderSystem.disableBlend();
        
        // Render boss name
        String bossName = entity.getDisplayName().getString();
        int nameX = screenWidth / 2;
        int nameY = y + (barHeight / 2) - 4;
        guiGraphics.drawCenteredString(mc.font, bossName, nameX, nameY, 0xFFFFFF);
    }
}
