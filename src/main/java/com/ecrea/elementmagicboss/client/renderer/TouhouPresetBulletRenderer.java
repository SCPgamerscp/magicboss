package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.TouhouPresetBulletProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.render.RenderHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TouhouPresetBulletRenderer extends EntityRenderer<TouhouPresetBulletProjectile> {
    /** 蝶の羽ばたき周期（tick） */
    private static final int BUTTERFLY_PERIOD = 5;
    /** 蝶の羽ばたき角度（度） */
    private static final float BUTTERFLY_ANGLE = 60.0f;

    public TouhouPresetBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }

    @Override
    public void render(TouhouPresetBulletProjectile entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float scale = entity.getVisualScale();
        poseStack.scale(scale, scale, scale);

        if (entity.getBulletType() == TouhouPresetBulletProjectile.BulletType.BUTTERFLY) {
            // 蝶: YH ButterflyProjectileType の移植
            // エンティティの向きに合わせて回転
            poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.lerp(partialTicks, entity.yRotO, entity.getYRot())));
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

            // 羽ばたき: |((tickCount + partialTicks) / period % 1) * 4 - 2| - 1
            // → -1 ～ +1 を周期的に繰り返す三角波
            float time = Math.abs((entity.tickCount + partialTicks) / BUTTERFLY_PERIOD % 1.0f * 4.0f - 2.0f) - 1.0f;

            ResourceLocation texture = getTextureLocation(entity);

            // 左の翼
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * BUTTERFLY_ANGLE));
            renderButterflyWing(poseStack, bufferSource, texture, false);
            poseStack.popPose();

            // 右の翼
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * -BUTTERFLY_ANGLE));
            renderButterflyWing(poseStack, bufferSource, texture, true);
            poseStack.popPose();
        } else {
            // 通常弾: ビルボード
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
            renderSprite(poseStack, bufferSource, getTextureLocation(entity));
        }

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    /**
     * 蝶の片翼を描画する（YH ButterflyProjectileType.Ins.tex() 移植）
     */
    private static void renderButterflyWing(PoseStack poseStack, MultiBufferSource bufferSource,
                                            ResourceLocation texture, boolean right) {
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(texture));

        // テクスチャUの範囲: 左翼 0.0-0.5, 右翼 0.5-1.0
        float u0 = right ? 0.5f : 0.0f;
        float u1 = right ? 1.0f : 0.5f;

        // XZ平面に展開（YH は x,0,y の頂点配置）
        butterflyVertex(poseMatrix, normalMatrix, consumer, u1 - 0.5f, 0.0f, 1.0f - 0.5f, u1, 0.0f);
        butterflyVertex(poseMatrix, normalMatrix, consumer, u1 - 0.5f, 0.0f, 0.0f - 0.5f, u1, 1.0f);
        butterflyVertex(poseMatrix, normalMatrix, consumer, u0 - 0.5f, 0.0f, 0.0f - 0.5f, u0, 1.0f);
        butterflyVertex(poseMatrix, normalMatrix, consumer, u0 - 0.5f, 0.0f, 1.0f - 0.5f, u0, 0.0f);
    }

    private static void butterflyVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                                        float x, float y, float z, float u, float v) {
        builder.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    private static void renderSprite(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation texture) {
        Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderHelper.CustomerRenderType.magic(texture));

        vertex(poseMatrix, normalMatrix, consumer, -0.5f, -0.5f, 0.0f, 0.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, -0.5f, 0.0f, 1.0f, 1.0f);
        vertex(poseMatrix, normalMatrix, consumer, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f);
        vertex(poseMatrix, normalMatrix, consumer, -0.5f, 0.5f, 0.0f, 0.0f, 0.0f);
    }

    private static void vertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                               float x, float y, float z, float u, float v) {
        builder.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normals, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(TouhouPresetBulletProjectile entity) {
        return new ResourceLocation(ElementMagicBossMod.MOD_ID, entity.getTexturePath());
    }
}
