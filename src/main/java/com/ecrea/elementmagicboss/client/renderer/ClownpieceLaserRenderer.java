package com.ecrea.elementmagicboss.client.renderer;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ClownpieceLaserEntity;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * クラウンピースレーザーレンダラー。
 * YH の DoubleLayerLaserType を依存なしで完全移植。
 * inner (TRANSPARENT) + outer (ADDITIVE) の2重描画で発光させる。
 */
public class ClownpieceLaserRenderer extends EntityRenderer<ClownpieceLaserEntity> {

    private static final ResourceLocation TEX_INNER =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/laser_inner.png");
    private static final ResourceLocation TEX_OUTER =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/entity/clownpiece/laser_outer.png");

    private static final int[] LASER_COLOR = { 0x6699FF, 0xFF6644 };

    public ClownpieceLaserRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public ResourceLocation getTextureLocation(ClownpieceLaserEntity e) { return TEX_INNER; }

    @Override
    public net.minecraft.world.phys.Vec3 getRenderOffset(ClownpieceLaserEntity e, float f) {
        // YH と同様にレーザーの視覚をエンティティ中心から開始する
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    @Override
    public boolean shouldRender(ClownpieceLaserEntity e,
            net.minecraft.client.renderer.culling.Frustum f, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(ClownpieceLaserEntity entity, float yaw, float partialTick,
                       PoseStack ps, MultiBufferSource buf, int light) {
        float opacity = entity.getOpacity(partialTick);
        if (opacity < 0.01f) return;
        if (entity.tickCount < 2) return;

        int col = LASER_COLOR[entity.laserType & 1];

        // YH: fade * 0xff → alpha
        int coreAlpha = (int)(opacity * 0xFF);
        double tranMul = opacity * 0.6; // laserTransparency default
        int tranAlpha  = (int)(tranMul * 0xFF);

        // inner core color: white with fade alpha
        int coreColor = (coreAlpha << 24) | 0xFFFFFF;
        // outer transparent color: colored with tran alpha
        int tranColor = (tranAlpha << 24) | (col & 0xFFFFFF);
        // outer additive color: RGB channel * tran (YH 加算)
        int addR = (int)((col >> 16 & 0xFF) * tranMul);
        int addG = (int)((col >>  8 & 0xFF) * tranMul);
        int addB = (int)((col       & 0xFF) * tranMul);
        int addColor = 0xFF000000 | addR << 16 | addG << 8 | addB;

        ps.pushPose();

        // YH ItemLaserRenderer と同様の回転
        ps.mulPose(Axis.YP.rotationDegrees(-entity.getViewYRot(partialTick)));
        ps.mulPose(Axis.XP.rotationDegrees(entity.getViewXRot(partialTick) + 90));

        // YH: scale(bbWidth * percentOpen, effectiveLength, bbWidth * percentOpen)
        // bbWidth=1.0, percentOpen=opacity でフェードイン/アウト
        float bbWidth = 0.5f;  // レーザーの太さ
        float effectiveLen = ClownpieceLaserEntity.LENGTH;
        ps.scale(bbWidth * opacity, effectiveLen, bbWidth * opacity);

        // YH DoubleLayerLaserType と同じ値 (s0=0.167f, s1=0.5f) をそのまま渡す
        var cache = buildCache(ps.last().pose(), 0.167f, 0.5f);

        // inner (TRANSPARENT)
        VertexConsumer vcTran = buf.getBuffer(ClownpieceRenderTypes.LASER_TRANSPARENT.apply(TEX_INNER));
        renderPart(false, vcTran, coreColor, cache.r0());
        renderPart(false, vcTran, tranColor, cache.r1());  // outer transparent layer

        // outer (ADDITIVE)
        VertexConsumer vcAdd = buf.getBuffer(ClownpieceRenderTypes.LASER_ADDITIVE.apply(TEX_OUTER));
        renderPart(false, vcAdd, addColor, cache.r1());

        ps.popPose();
        super.render(entity, yaw, partialTick, ps, buf, light);
    }

    // -------- YH DoubleLayerLaserType geometry --------

    private record Cache(float[][] r0, float[][] r1) {}

    private static Cache buildCache(Matrix4f mat, float s0, float s1) {
        var p0 = new Vector4f(0, 0, 0, 1).mul(mat);
        var px = new Vector4f(1, 0, 0, 0).mul(mat);
        var py = new Vector4f(0, 1, 0, 0).mul(mat);
        var pz = new Vector4f(0, 0, 1, 0).mul(mat);
        var r0 = new float[8][3]; fill(r0, p0, px, py, pz, s0);
        var r1 = new float[8][3]; fill(r1, p0, px, py, pz, s1);
        return new Cache(r0, r1);
    }

    private static void fill(float[][] arr, Vector4f p0, Vector4f px, Vector4f py, Vector4f pz, float s) {
        calc(arr[0], p0, px, pz, -s, -s); calc(arr[1], p0, px, pz,  s, -s);
        calc(arr[2], p0, px, pz, -s,  s); calc(arr[3], p0, px, pz,  s,  s);
        add(arr[4], arr[0], py); add(arr[5], arr[1], py);
        add(arr[6], arr[2], py); add(arr[7], arr[3], py);
    }

    private static void calc(float[] a, Vector4f p0, Vector4f px, Vector4f pz, float sx, float sz) {
        a[0] = p0.x + px.x*sx + pz.x*sz;
        a[1] = p0.y + px.y*sx + pz.y*sz;
        a[2] = p0.z + px.z*sx + pz.z*sz;
    }

    private static void add(float[] a, float[] base, Vector4f p) {
        a[0] = base[0]+p.x; a[1] = base[1]+p.y; a[2] = base[2]+p.z;
    }

    /** YH: renderPart → 4 quads (面4枚) */
    private static void renderPart(boolean invert, VertexConsumer vc, int color, float[][] arr) {
        renderQuad(invert, vc, color, arr, 0, 2);
        renderQuad(invert, vc, color, arr, 3, 1);
        renderQuad(invert, vc, color, arr, 2, 3);
        renderQuad(invert, vc, color, arr, 1, 0);
    }

    private static void renderQuad(boolean invert, VertexConsumer vc, int col, float[][] arr, int i0, int i1) {
        if (invert) {
            addV(vc, col, arr[i1+4], 0, 0); addV(vc, col, arr[i1], 0, 1);
            addV(vc, col, arr[i0],   1, 1); addV(vc, col, arr[i0+4], 1, 0);
        } else {
            addV(vc, col, arr[i0+4], 1, 0); addV(vc, col, arr[i0], 1, 1);
            addV(vc, col, arr[i1],   0, 1); addV(vc, col, arr[i1+4], 0, 0);
        }
    }

    private static void addV(VertexConsumer vc, int col, float[] p, float u, float v) {
        int a = (col >> 24) & 0xFF;
        int r = (col >> 16) & 0xFF;
        int g = (col >>  8) & 0xFF;
        int b =  col        & 0xFF;
        // POSITION_TEX_COLOR: position + uv + color のみ (uv2/normal は不要)
        vc.vertex(p[0], p[1], p[2]).uv(u, v).color(r, g, b, a).endVertex();
    }
}
