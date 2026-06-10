package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.DanmakuShotProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * 弾幕雨専用レンダラー。
 * 弾幕起こしのようにvariantごとに色とりどりにしつつ、
 * 水色・シアン・アクア系のパレットで統一。
 */
public class WaterDanmakuProjectileRenderer extends EntityRenderer<DanmakuShotProjectile> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/danmaku_shot_projectile.png");
    private static final int FRAME_COUNT = 13;

    /**
     * 水色系の色とりどりパレット（13色）
     * 弾幕起こしの炎パレットの代わりに、水・氷・空をイメージした
     * シアン～水色～ラベンダー～ミント系統の13色。
     */
    private static final int[][] WATER_PALETTE = {
        {  80, 200, 255},  //  0: 明るいスカイブルー
        { 100, 220, 240},  //  1: ライトシアン
        {  60, 180, 255},  //  2: アクアブルー
        { 130, 230, 255},  //  3: ペールアクア
        {  50, 160, 230},  //  4: オーシャンブルー
        { 120, 240, 220},  //  5: ミントグリーン
        { 160, 210, 255},  //  6: ラベンダーブルー
        {  70, 190, 240},  //  7: ターコイズ
        { 140, 255, 255},  //  8: ブライトシアン
        {  90, 170, 250},  //  9: コーンフラワーブルー
        { 110, 230, 200},  // 10: シーフォーム
        { 170, 220, 255},  // 11: ベイビーブルー
        {  40, 200, 220},  // 12: ティールシアン
    };

    public WaterDanmakuProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DanmakuShotProjectile entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        poseStack.scale(1.05F, 1.05F, 1.05F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderSprite(poseStack, bufferSource, entity.getVariant());
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private static void renderSprite(PoseStack poseStack, MultiBufferSource bufferSource, int variant) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(TEXTURE));

        float minU = variant / (float) FRAME_COUNT;
        float maxU = (variant + 1) / (float) FRAME_COUNT;

        int colorIndex = Math.floorMod(variant, WATER_PALETTE.length);
        int r = WATER_PALETTE[colorIndex][0];
        int g = WATER_PALETTE[colorIndex][1];
        int b = WATER_PALETTE[colorIndex][2];

        vertex(poseMatrix, normalMatrix, consumer, -0.5F, -0.5F, 0.0F, minU, 1.0F, r, g, b);
        vertex(poseMatrix, normalMatrix, consumer,  0.5F, -0.5F, 0.0F, maxU, 1.0F, r, g, b);
        vertex(poseMatrix, normalMatrix, consumer,  0.5F,  0.5F, 0.0F, maxU, 0.0F, r, g, b);
        vertex(poseMatrix, normalMatrix, consumer, -0.5F,  0.5F, 0.0F, minU, 0.0F, r, g, b);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v,
                               int r, int g, int b) {
        builder.vertex(matrix, x, y, z)
                .color(r, g, b, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DanmakuShotProjectile entity) {
        return TEXTURE;
    }
}
