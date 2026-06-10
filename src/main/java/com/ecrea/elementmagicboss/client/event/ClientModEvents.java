package com.ecrea.elementmagicboss.client.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.client.renderer.*;
import com.ecrea.elementmagicboss.entity.ModEntities;
import io.redspace.ironsspellbooks.entity.spells.magma_ball.MagmaBallRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.ecrea.elementmagicboss.client.renderer.ClownpieceBulletRenderer;
import com.ecrea.elementmagicboss.client.renderer.ClownpieceLaserRenderer;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SKELETON_KING.get(), SkeletonKingRenderer::new);
        event.registerEntityRenderer(ModEntities.DANMAKU_SHOT_PROJECTILE.get(), DanmakuShotProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.WATER_DANMAKU_PROJECTILE.get(), WaterDanmakuProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.BLAZING_STAR_METEOR.get(), BlazingStarMeteorRenderer::new);
        event.registerEntityRenderer(ModEntities.BLOOD_RAIN_NEEDLE.get(), BloodRainNeedleRenderer::new);
        event.registerEntityRenderer(ModEntities.SEA_OF_FIRE_BOMB.get(), MagmaBallRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_FRYING_PAN.get(), FireFryingPanRenderer::new);
        event.registerEntityRenderer(ModEntities.MAGIC_IMPACT_CROSS.get(), MagicImpactCrossRenderer::new);
        event.registerEntityRenderer(ModEntities.ASCENSION_CROSS.get(), AscensionCrossRenderer::new);
        event.registerEntityRenderer(ModEntities.CRIMSON_YOUNG_MOON.get(), CrimsonYoungMoonRenderer::new);
        event.registerEntityRenderer(ModEntities.MESH_OF_LIGHT_AND_DARKNESS_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.MESH_OF_LIGHT_AND_DARKNESS_LASER.get(), MeshOfLightAndDarknessLaserRenderer::new);
        event.registerEntityRenderer(ModEntities.TOUHOU_PRESET_BULLET.get(), TouhouPresetBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.TOUHOU_LASER.get(), TouhouLaserRenderer::new);
        event.registerEntityRenderer(ModEntities.INNATE_DREAM_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.MASTER_SPARK_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.INHERITED_RITUAL_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.MIDNIGHT_CHORUS_MASTER_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.YOUKAI_POLYGRAPH_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.DOUBLE_BLACK_DEATH_BUTTERFLY_CONTROLLER.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.SCARLET_MEISTER_SWEEP.get(), ScarletMeisterSweepRenderer::new);
        event.registerEntityRenderer(ModEntities.TIME_STOP_FIELD.get(), TimeStopFieldRenderer::new);
        event.registerEntityRenderer(ModEntities.FIRE_FRYING_PAN_PROJECTILE.get(), FireFryingPanProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.CRIMSON_YOUNG_MOON_PROJECTILE.get(), CrimsonYoungMoonProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.YUKARI_LASER_DANMAKU_PROJECTILE.get(), YukariLaserDanmakuRenderer::new);
        event.registerEntityRenderer(ModEntities.SCARLET_MEISTER_PROJECTILE.get(), ScarletMeisterProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.POISON_SPLASH_STORM_FIELD.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.DANMAKU_MEISTER_SWEEP.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.DANMAKU_MEISTER_PROJECTILE.get(), DanmakuMeisterProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.RAISE_DANMAKU_PROJECTILE.get(), RaiseDanmakuProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.SPINNING_SWORD.get(), SpinningSwordRenderer::new);
        event.registerEntityRenderer(ModEntities.TRIDENT_BOMB.get(), TridentBombRenderer::new);
        event.registerEntityRenderer(ModEntities.TRIDENT_SHARD.get(), TridentShardRenderer::new);
        event.registerEntityRenderer(ModEntities.ARROW_STORM_ARROW.get(), ArrowStormArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.TRUE_ARROW_VOLLEY.get(), TrueArrowVolleyRenderer::new);
        event.registerEntityRenderer(ModEntities.HYPER_BLACK_HOLE.get(), HyperBlackHoleRenderer::new);
        event.registerEntityRenderer(ModEntities.HYPER_BLACK_HOLE_MISSILE.get(), HyperBlackHoleMissileRenderer::new);
        // 繧ｯ繝ｩ繧ｦ繝ｳ繝斐・繧ｹ: 繝輔ぅ繝ｼ繝ｫ繝・荳榊庄隕・縺ｯNoopRenderer縲∝ｼｾ荳ｸ縺ｯClownpieceBulletRenderer
        event.registerEntityRenderer(ModEntities.CLOWNPIECE_FIELD.get(), ctx -> new net.minecraft.client.renderer.entity.NoopRenderer<>(ctx));
        event.registerEntityRenderer(ModEntities.CLOWNPIECE_BULLET.get(), ClownpieceBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.CLOWNPIECE_LASER.get(), ClownpieceLaserRenderer::new);
        event.registerEntityRenderer(ModEntities.APOSTLE_BEAM_CROSS.get(), ApostleBeamCrossRenderer::new);
        event.registerEntityRenderer(ModEntities.ABYSS_BLAST.get(), AbyssBlastRenderer::new);
        event.registerEntityRenderer(ModEntities.SLIME_BLOCK_PROJECTILE.get(), SlimeBlockRenderer::new);
        event.registerEntityRenderer(ModEntities.HEALING_CRYSTAL.get(), HealingCrystalRenderer::new);
    }
}


