package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.TouhouLaserEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class TouhouLaserRenderer extends EntityRenderer<TouhouLaserEntity> {
    private static final ResourceLocation TEX_INNER =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/laser_inner.png");
    private static final ResourceLocation TEX_OUTER =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/laser_outer.png");

    public TouhouLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(TouhouLaserEntity entity) {
        return TEX_INNER;
    }

    @Override
    public boolean shouldRender(TouhouLaserEntity entity,
                                net.minecraft.client.renderer.culling.Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(TouhouLaserEntity entity, float yaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float opacity = entity.getOpacity(partialTick);
        if (opacity < 0.01f) {
            return;
        }

        int color = entity.getLaserColor().rgb();
        int coreAlpha = (int) (opacity * 0xFF);
        double tranMul = opacity * 0.65D;
        int tranAlpha = (int) (tranMul * 0xFF);

        int coreColor = (coreAlpha << 24) | 0xFFFFFF;
        int transparentColor = (tranAlpha << 24) | (color & 0xFFFFFF);
        int additiveR = (int) ((color >> 16 & 0xFF) * tranMul);
        int additiveG = (int) ((color >> 8 & 0xFF) * tranMul);
        int additiveB = (int) ((color & 0xFF) * tranMul);
        int additiveColor = 0xFF000000 | additiveR << 16 | additiveG << 8 | additiveB;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getViewYRot(partialTick)));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTick) + 90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getRollSeed() * 360.0F));
        poseStack.scale(0.55F * opacity, entity.getRenderLength(), 0.55F * opacity);

        var cache = buildCache(poseStack.last().pose(), 0.167f, 0.5f);
        VertexConsumer transparent = bufferSource.getBuffer(ClownpieceRenderTypes.LASER_TRANSPARENT.apply(TEX_INNER));
        renderPart(transparent, coreColor, cache.r0());
        renderPart(transparent, transparentColor, cache.r1());

        VertexConsumer additive = bufferSource.getBuffer(ClownpieceRenderTypes.LASER_ADDITIVE.apply(TEX_OUTER));
        renderPart(additive, additiveColor, cache.r1());

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private record Cache(float[][] r0, float[][] r1) {}

    private static Cache buildCache(Matrix4f mat, float s0, float s1) {
        var p0 = new Vector4f(0, 0, 0, 1).mul(mat);
        var px = new Vector4f(1, 0, 0, 0).mul(mat);
        var py = new Vector4f(0, 1, 0, 0).mul(mat);
        var pz = new Vector4f(0, 0, 1, 0).mul(mat);
        var r0 = new float[8][3];
        fill(r0, p0, px, py, pz, s0);
        var r1 = new float[8][3];
        fill(r1, p0, px, py, pz, s1);
        return new Cache(r0, r1);
    }

    private static void fill(float[][] arr, Vector4f p0, Vector4f px, Vector4f py, Vector4f pz, float s) {
        calc(arr[0], p0, px, pz, -s, -s);
        calc(arr[1], p0, px, pz, s, -s);
        calc(arr[2], p0, px, pz, -s, s);
        calc(arr[3], p0, px, pz, s, s);
        add(arr[4], arr[0], py);
        add(arr[5], arr[1], py);
        add(arr[6], arr[2], py);
        add(arr[7], arr[3], py);
    }

    private static void calc(float[] a, Vector4f p0, Vector4f px, Vector4f pz, float sx, float sz) {
        a[0] = p0.x + px.x * sx + pz.x * sz;
        a[1] = p0.y + px.y * sx + pz.y * sz;
        a[2] = p0.z + px.z * sx + pz.z * sz;
    }

    private static void add(float[] a, float[] base, Vector4f p) {
        a[0] = base[0] + p.x;
        a[1] = base[1] + p.y;
        a[2] = base[2] + p.z;
    }

    private static void renderPart(VertexConsumer vc, int color, float[][] arr) {
        renderQuad(vc, color, arr, 0, 2);
        renderQuad(vc, color, arr, 3, 1);
        renderQuad(vc, color, arr, 2, 3);
        renderQuad(vc, color, arr, 1, 0);
    }

    private static void renderQuad(VertexConsumer vc, int color, float[][] arr, int i0, int i1) {
        addV(vc, color, arr[i0 + 4], 1, 0);
        addV(vc, color, arr[i0], 1, 1);
        addV(vc, color, arr[i1], 0, 1);
        addV(vc, color, arr[i1 + 4], 0, 0);
    }

    private static void addV(VertexConsumer vc, int color, float[] p, float u, float v) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        vc.vertex(p[0], p[1], p[2]).uv(u, v).color(r, g, b, a).endVertex();
    }
}
