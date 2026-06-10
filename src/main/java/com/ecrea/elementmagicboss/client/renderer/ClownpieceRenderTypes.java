package com.ecrea.elementmagicboss.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * クラウンピース弾幕/レーザー専用の RenderType ファクトリ。
 * RenderType を継承して protected フィールドにアクセスする。
 */
public abstract class ClownpieceRenderTypes extends RenderType {

    // dummy constructor — インスタンス化しない
    private ClownpieceRenderTypes(String n, VertexFormat f, VertexFormat.Mode m, int b, boolean c, boolean s, Runnable u, Runnable d) {
        super(n, f, m, b, c, s, u, d);
    }

    /** ADDITIVE ビルボード (弾幕) */
    public static final Function<ResourceLocation, RenderType> BULLET_ADDITIVE =
            Util.memoize(tex -> RenderType.create(
                    "cp_bullet_add_" + tex,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS, 256, true, false,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new TextureStateShard(tex, false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)));

    /** TRANSPARENT ビルボード (弾幕) */
    public static final Function<ResourceLocation, RenderType> BULLET_TRANSPARENT =
            Util.memoize(tex -> RenderType.create(
                    "cp_bullet_tran_" + tex,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS, 256, true, true,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new TextureStateShard(tex, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(NO_CULL)
                            .createCompositeState(false)));

    /** TRANSPARENT レーザー */
    public static final Function<ResourceLocation, RenderType> LASER_TRANSPARENT =
            Util.memoize(tex -> RenderType.create(
                    "cp_laser_tran_" + tex,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS, 256, true, true,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new TextureStateShard(tex, false, false))
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(CULL)
                            .createCompositeState(false)));

    /** ADDITIVE レーザー */
    public static final Function<ResourceLocation, RenderType> LASER_ADDITIVE =
            Util.memoize(tex -> RenderType.create(
                    "cp_laser_add_" + tex,
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS, 256, true, false,
                    CompositeState.builder()
                            .setShaderState(new ShaderStateShard(GameRenderer::getPositionTexColorShader))
                            .setTextureState(new TextureStateShard(tex, false, false))
                            .setTransparencyState(ADDITIVE_TRANSPARENCY)
                            .setCullState(CULL)
                            .createCompositeState(false)));
}
