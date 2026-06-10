package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.SpinningSwordEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 剣の舞 - 鉄剣をその場でスピンさせるレンダラー
 */
public class SpinningSwordRenderer extends EntityRenderer<SpinningSwordEntity> {
    private final ItemRenderer itemRenderer;
    private static final ItemStack SWORD = new ItemStack(Items.IRON_SWORD);

    public SpinningSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(SpinningSwordEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // スムーズ補間で回転角度を計算
        float spin = entity.prevSpin + (entity.spin - entity.prevSpin) * partialTick;

        // Y軸回転（スピン）
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        // 剣を縦に立てる
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        // 少し大きめに表示
        poseStack.scale(1.5f, 1.5f, 1.5f);

        itemRenderer.renderStatic(
                SWORD,
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SpinningSwordEntity entity) {
        return new ResourceLocation("minecraft", "textures/item/iron_sword.png");
    }
}
