package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.client.model.FireSwordModel;
import com.ecrea.elementmagicboss.item.FireSwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class FireSwordRenderer extends GeoItemRenderer<FireSwordItem> {
    public FireSwordRenderer() {
        super(new FireSwordModel());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.scale(1.5f, 1.5f, 1.5f);
        super.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
