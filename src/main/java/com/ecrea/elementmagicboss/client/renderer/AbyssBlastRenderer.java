package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.AbyssBlastEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * アビスブラストのビームレンダラー。
 * Cataclysm の Abyss_Blast_Renderer を移植。
 */
@OnlyIn(Dist.CLIENT)
public class AbyssBlastRenderer extends EntityRenderer<AbyssBlastEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/abyss_laser_beam.png");
    private static final float TEXTURE_WIDTH = 256F;
    private static final float TEXTURE_HEIGHT = 32F;
    private static final float START_RADIUS = 1.0F;
    private static final float END_RADIUS = 1.15F;
    private static final float BEAM_RADIUS = 1.0F;
    private boolean clearerView = false;

    public AbyssBlastRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssBlastEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(AbyssBlastEntity beam, Frustum camera, double camX, double camY, double camZ) {
        return true;
    }

    @Override
    public void render(AbyssBlastEntity beam, float entityYaw, float delta,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // caster未設定の最初のフレームでは描画しない（横向きビーム防止）
        if (beam.caster == null) return;

        clearerView = beam.caster instanceof Player
                && Minecraft.getInstance().player == beam.caster
                && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;

        double collidePosX = beam.prevCollidePosX + (beam.collidePosX - beam.prevCollidePosX) * delta;
        double collidePosY = beam.prevCollidePosY + (beam.collidePosY - beam.prevCollidePosY) * delta;
        double collidePosZ = beam.prevCollidePosZ + (beam.collidePosZ - beam.prevCollidePosZ) * delta;
        double posX = beam.xo + (beam.getX() - beam.xo) * delta;
        double posY = beam.yo + (beam.getY() - beam.yo) * delta;
        double posZ = beam.zo + (beam.getZ() - beam.zo) * delta;
        float yaw = beam.prevYaw + (beam.renderYaw - beam.prevYaw) * delta;
        float pitch = beam.prevPitch + (beam.renderPitch - beam.prevPitch) * delta;

        float length = (float) Math.sqrt(
                Math.pow(collidePosX - posX, 2) +
                Math.pow(collidePosY - posY, 2) +
                Math.pow(collidePosZ - posZ, 2));

        // 長さ0なら描画しない
        if (length < 0.1F) return;

        int frame = Mth.floor((beam.getAppearTimer() - 1 + delta) * 2);
        if (frame < 0) frame = 6;

        // entityTranslucentEmissive で常に明るく光る描画
        VertexConsumer builder = bufferSource.getBuffer(
                RenderType.entityTranslucentEmissive(getTextureLocation(beam)));

        renderBeam(length, 180F / (float) Math.PI * yaw, 180F / (float) Math.PI * pitch,
                frame, poseStack, builder, packedLight);

        poseStack.pushPose();
        poseStack.translate(collidePosX - posX, collidePosY - posY, collidePosZ - posZ);
        renderEnd(frame, beam.blockSide, poseStack, builder, packedLight);
        poseStack.popPose();
    }

    private void renderFlatQuad(int frame, PoseStack poseStack, VertexConsumer builder, int packedLight) {
        float minU = 16F / TEXTURE_WIDTH * frame;
        float minV = 0F;
        float maxU = minU + 16F / TEXTURE_WIDTH;
        float maxV = minV + 16F / TEXTURE_HEIGHT;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat4 = pose.pose();
        Matrix3f mat3 = pose.normal();
        drawVertex(mat4, mat3, builder, -START_RADIUS, -END_RADIUS, 0, minU, minV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, -START_RADIUS, END_RADIUS, 0, minU, maxV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, START_RADIUS, END_RADIUS, 0, maxU, maxV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, START_RADIUS, -END_RADIUS, 0, maxU, minV, 1F, packedLight);
    }

    private void renderEnd(int frame, Direction side, PoseStack poseStack, VertexConsumer builder, int packedLight) {
        poseStack.pushPose();
        Quaternionf quat = this.entityRenderDispatcher.cameraOrientation();
        poseStack.mulPose(quat);
        renderFlatQuad(frame, poseStack, builder, packedLight);
        poseStack.popPose();

        if (side != null) {
            poseStack.pushPose();
            Quaternionf sideQuat = side.getRotation();
            sideQuat.mul(quatFromRotationXYZ(90, 0, 0));
            poseStack.mulPose(sideQuat);
            poseStack.translate(0, 0, -0.01F);
            renderFlatQuad(frame, poseStack, builder, packedLight);
            poseStack.popPose();
        }
    }

    private void drawBeam(float length, int frame, PoseStack poseStack, VertexConsumer builder, int packedLight) {
        // UV座標計算 ── float除算を保証
        float minU = 0F;
        float minV = 16F / TEXTURE_HEIGHT + 1F / TEXTURE_HEIGHT * frame;
        float maxU = minU + 20F / TEXTURE_WIDTH;
        float maxV = minV + 1F / TEXTURE_HEIGHT;
        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat4 = pose.pose();
        Matrix3f mat3 = pose.normal();
        float offset = clearerView ? -1F : 0F;
        drawVertex(mat4, mat3, builder, -BEAM_RADIUS, offset, 0, minU, minV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, -BEAM_RADIUS, length, 0, minU, maxV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, BEAM_RADIUS, length, 0, maxU, maxV, 1F, packedLight);
        drawVertex(mat4, mat3, builder, BEAM_RADIUS, offset, 0, maxU, minV, 1F, packedLight);
    }

    private void renderBeam(float length, float yaw, float pitch, int frame,
                            PoseStack poseStack, VertexConsumer builder, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(quatFromRotationXYZ(90, 0, 0));
        poseStack.mulPose(quatFromRotationXYZ(0, 0, yaw - 90F));
        poseStack.mulPose(quatFromRotationXYZ(-pitch, 0, 0));
        poseStack.pushPose();
        if (!clearerView) {
            poseStack.mulPose((new Quaternionf()).rotationY(
                    (Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() + 90)));
        }
        drawBeam(length, frame, poseStack, builder, packedLight);
        poseStack.popPose();

        if (!clearerView) {
            poseStack.pushPose();
            poseStack.mulPose((new Quaternionf()).rotationY(
                    (-Minecraft.getInstance().gameRenderer.getMainCamera().getXRot() - 90)
                            * ((float) Math.PI / 180F)));
            drawBeam(length, frame, poseStack, builder, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void drawVertex(Matrix4f matrix, Matrix3f normals, VertexConsumer builder,
                            float x, float y, float z, float u, float v, float alpha, int packedLight) {
        builder.vertex(matrix, x, y, z)
                .color(255, 255, 255, (int) (255 * alpha))
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(0xF000F0) // フルブライト
                .normal(normals, 0F, 1F, 0F)
                .endVertex();
    }

    /** CMMathUtil.quatFromRotationXYZ の移植 (degrees版) */
    private static Quaternionf quatFromRotationXYZ(float xDeg, float yDeg, float zDeg) {
        float x = xDeg * ((float) Math.PI / 180F);
        float y = yDeg * ((float) Math.PI / 180F);
        float z = zDeg * ((float) Math.PI / 180F);
        return (new Quaternionf()).rotationXYZ(x, y, z);
    }
}
