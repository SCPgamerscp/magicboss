package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.entity.projectile.TridentShardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TridentShardRenderer extends EntityRenderer<TridentShardEntity> {

    private static final ResourceLocation TRIDENT_TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/trident.png");

    private final TridentModel model;

    public TridentShardRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.model = new TridentModel(ctx.bakeLayer(ModelLayers.TRIDENT));
    }

    @Override
    public void render(TridentShardEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // バニラ ThrownTridentRenderer と完全に同じ式
        poseStack.mulPose(Axis.YP.rotationDegrees(
                Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 90.0F));

        poseStack.scale(0.8f, 0.8f, 0.8f);

        VertexConsumer vc = buffer.getBuffer(model.renderType(TRIDENT_TEXTURE));
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);

        VertexConsumer glint = buffer.getBuffer(RenderType.entityGlintDirect());
        model.renderToBuffer(poseStack, glint, packedLight, OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TridentShardEntity entity) {
        return TRIDENT_TEXTURE;
    }
}
