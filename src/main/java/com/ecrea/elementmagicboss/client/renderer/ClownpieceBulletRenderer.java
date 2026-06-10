package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ClownpieceBulletEntity;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * クラウンピース弾幕レンダラー。
 * YH と同様に加算合成（ADDITIVE）で描画し発光させる。
 * bulletType 0/1 = MENTOSビルボード (加算)
 * bulletType 2   = STAR      (半透明)
 * bulletType 3   = SPARK     (加算)
 */
public class ClownpieceBulletRenderer extends EntityRenderer<ClownpieceBulletEntity> {

    // 各テクスチャの ResourceLocation
    private static final ResourceLocation TEX_MENTOS_BLUE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/mentos_blue.png");
    private static final ResourceLocation TEX_MENTOS_RED =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/mentos_red.png");
    private static final ResourceLocation TEX_STAR_RED =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/star_red.png");
    private static final ResourceLocation TEX_SPARK_BLUE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/spark_blue.png");

    // ADDITIVE RenderType (YH と同じ: POSITION_TEX_COLOR + ADDITIVE_TRANSPARENCY)
    // TRANSPARENT RenderType (半透明)

    public ClownpieceBulletRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(ClownpieceBulletEntity entity) {
        return switch (entity.bulletType & 3) {
            case 1  -> TEX_MENTOS_RED;
            case 2  -> TEX_STAR_RED;
            case 3  -> TEX_SPARK_BLUE;
            default -> TEX_MENTOS_BLUE;
        };
    }

    @Override
    public void render(ClownpieceBulletEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        ResourceLocation tex = getTextureLocation(entity);
        int bt = entity.bulletType & 3;

        // bulletType 2 (STAR) は半透明、それ以外は加算
        RenderType renderType = (bt == 2)
                ? ClownpieceRenderTypes.BULLET_TRANSPARENT.apply(tex)
                : ClownpieceRenderTypes.BULLET_ADDITIVE.apply(tex);

        poseStack.pushPose();
        // ビルボード: カメラ方向へ
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        // サイズ (YH: MENTOS size=2, STAR size=2, SPARK size=1) → 大きめに調整
        float s = (bt == 3) ? 0.35f : 0.45f;
        poseStack.scale(s, s, s);

        VertexConsumer vc = bufferSource.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();

        // ADDITIVE では alpha = grayvalue (YH の fading ロジックに相当, ここでは 0xFF 固定)
        quad(vc, pose, 0xFF, 0xFF, 0xFF, 0xFF);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void quad(VertexConsumer vc, PoseStack.Pose pose,
                               int r, int g, int b, int a) {
        var m = pose.pose();
        // POSITION_TEX_COLOR: position + uv + color のみ (uv2/normal は不要)
        vc.vertex(m, -0.5f, -0.5f, 0).uv(1, 1).color(r, g, b, a).endVertex();
        vc.vertex(m,  0.5f, -0.5f, 0).uv(0, 1).color(r, g, b, a).endVertex();
        vc.vertex(m,  0.5f,  0.5f, 0).uv(0, 0).color(r, g, b, a).endVertex();
        vc.vertex(m, -0.5f,  0.5f, 0).uv(1, 0).color(r, g, b, a).endVertex();
    }
}
