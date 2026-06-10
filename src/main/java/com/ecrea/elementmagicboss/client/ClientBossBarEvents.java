package com.ecrea.elementmagicboss.client;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, value = Dist.CLIENT)
public class ClientBossBarEvents {
    private static final ResourceLocation WIZARD_EMPTY = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_empty.png");
    private static final ResourceLocation WIZARD_FULL = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_full.png");
    private static final ResourceLocation RAZIEL_EMPTY = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/angel_raziel_empty.png");
    private static final ResourceLocation RAZIEL_FULL = new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/angel_raziel_full.png");

    // エレメントボス用テクスチャサイズ (256x170)
    private static final int WIZARD_TEX_WIDTH = 256;
    private static final int WIZARD_TEX_HEIGHT = 170;
    private static final int WIZARD_DISPLAY_WIDTH = 200;
    private static final int WIZARD_DISPLAY_HEIGHT = 133;

    // ラジエルボス用テクスチャサイズ (768x192)
    private static final int RAZIEL_TEX_WIDTH = 768;
    private static final int RAZIEL_TEX_HEIGHT = 192;
    private static final int RAZIEL_DISPLAY_WIDTH = 300;
    private static final int RAZIEL_DISPLAY_HEIGHT = 75;

    private static final int PADDING = 5; 

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        String nameStr = event.getBossEvent().getName().getString().toLowerCase();
        
        if (nameStr.contains("大魔法使いエレメント") || nameStr.contains("legendary wizard")) {
            event.setCanceled(true);
            drawCustomBar(event, WIZARD_EMPTY, WIZARD_FULL, 
                WIZARD_TEX_WIDTH, WIZARD_TEX_HEIGHT, 
                WIZARD_DISPLAY_WIDTH, WIZARD_DISPLAY_HEIGHT, 
                -40, 85);
            event.setIncrement(WIZARD_DISPLAY_HEIGHT + PADDING - 40);
        } else if (nameStr.contains("天使ラジエル") || nameStr.contains("angel raziel")) {
            event.setCanceled(true);
            drawCustomBar(event, RAZIEL_EMPTY, RAZIEL_FULL, 
                RAZIEL_TEX_WIDTH, RAZIEL_TEX_HEIGHT, 
                RAZIEL_DISPLAY_WIDTH, RAZIEL_DISPLAY_HEIGHT, 
                -20, 55);
            event.setIncrement(RAZIEL_DISPLAY_HEIGHT + PADDING);
        }
    }

    private static void drawCustomBar(CustomizeGuiOverlayEvent.BossEventProgress event, 
            ResourceLocation empty, ResourceLocation full, 
            int texWidth, int texHeight, 
            int displayWidth, int displayHeight,
            int offsetY, int textOffsetY) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        
        int y = event.getY() + offsetY;
        int x = (guiGraphics.guiWidth() - displayWidth) / 2;

        poseStack.pushPose();
        
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. 背景
        guiGraphics.blit(empty, x, y, displayWidth, displayHeight, 0, 0, texWidth, texHeight, texWidth, texHeight);

        // 2. ゲージ
        float progress = event.getBossEvent().getProgress();
        if (progress > 0) {
            int uWidth = (int)(texWidth * progress);
            int destWidth = (int)(displayWidth * progress);

            if (destWidth > 0) {
                guiGraphics.blit(full,
                        x, y,
                        destWidth, displayHeight, 
                        0, 0,
                        uWidth, texHeight,
                        texWidth, texHeight);
            }
        }

        // 3. 名前
        Component component = event.getBossEvent().getName();
        int textWidth = Minecraft.getInstance().font.width(component);
        int textX = guiGraphics.guiWidth() / 2 - textWidth / 2;
        int textY = y + textOffsetY; // デザインに合わせて位置調整
        
        guiGraphics.drawString(Minecraft.getInstance().font, component, textX, textY, 16777215, true);

        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}