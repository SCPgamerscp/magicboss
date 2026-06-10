package com.ecrea.elementmagicboss.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.Function;

/**
 * アビスブラスト専用の RenderType。
 * Cataclysm の CMRenderTypes.GLOWING_EFFECT を移植。
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbyssBlastRenderTypes extends RenderType {

    private AbyssBlastRenderTypes(String n, VertexFormat f, VertexFormat.Mode m,
                                  int b, boolean c, boolean s, Runnable u, Runnable d) {
        super(n, f, m, b, c, s, u, d);
    }

    public static final Function<ResourceLocation, RenderType> GLOWING_EFFECT = Util.memoize(
            tex -> {
                CompositeState state = CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOverlayState(OVERLAY)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(false);
                return create("abyss_glow_effect", DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS, 256, true, true, state);
            }
    );

    public static RenderType getGlowingEffect(ResourceLocation location) {
        return GLOWING_EFFECT.apply(location);
    }
}
