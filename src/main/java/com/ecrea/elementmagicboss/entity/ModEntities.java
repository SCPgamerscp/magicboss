package com.ecrea.elementmagicboss.entity;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ArrowStormArrow;
import com.ecrea.elementmagicboss.entity.projectile.ClownpieceFieldEntity;
import com.ecrea.elementmagicboss.entity.projectile.HyperBlackHoleEntity;
import com.ecrea.elementmagicboss.entity.projectile.HyperBlackHoleMissile;
import com.ecrea.elementmagicboss.entity.projectile.*;
import com.ecrea.elementmagicboss.entity.projectile.SpinningSwordEntity;
import com.ecrea.elementmagicboss.entity.projectile.TridentBombProjectile;
import com.ecrea.elementmagicboss.entity.projectile.TridentShardEntity;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<EntityType<ElementEntity>> ELEMENT_ENTITY =
            ENTITIES.register("element_entity",
                    () -> EntityType.Builder.of(ElementEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "element_entity").toString()));

    public static final RegistryObject<EntityType<AngelRazielEntity>> ANGEL_RAZIEL =
            ENTITIES.register("angel_raziel",
                    () -> EntityType.Builder.of(AngelRazielEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "angel_raziel").toString()));

    public static final RegistryObject<EntityType<SkeletonKingEntity>> SKELETON_KING =
            ENTITIES.register("skeleton_king",
                    () -> EntityType.Builder.of(SkeletonKingEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "skeleton_king").toString()));

    public static final RegistryObject<EntityType<DanmakuShotProjectile>> DANMAKU_SHOT_PROJECTILE =
            ENTITIES.register("danmaku_shot_projectile",
                    () -> EntityType.Builder.<DanmakuShotProjectile>of(DanmakuShotProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_shot_projectile").toString()));

    public static final RegistryObject<EntityType<WaterDanmakuProjectile>> WATER_DANMAKU_PROJECTILE =
            ENTITIES.register("water_danmaku_projectile",
                    () -> EntityType.Builder.<WaterDanmakuProjectile>of(WaterDanmakuProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "water_danmaku_projectile").toString()));

    public static final RegistryObject<EntityType<BlazingStarMeteorProjectile>> BLAZING_STAR_METEOR =
            ENTITIES.register("blazing_star_meteor",
                    () -> EntityType.Builder.<BlazingStarMeteorProjectile>of(BlazingStarMeteorProjectile::new, MobCategory.MISC)
                            .sized(0.9f, 0.9f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "blazing_star_meteor").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<BloodRainNeedle>> BLOOD_RAIN_NEEDLE =
            ENTITIES.register("blood_rain_needle",
                    () -> EntityType.Builder.<BloodRainNeedle>of(
                                    (EntityType.EntityFactory<BloodRainNeedle>) (type, level) ->
                                            new BloodRainNeedle((EntityType<? extends BloodNeedle>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "blood_rain_needle").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<SeaOfFireBomb>> SEA_OF_FIRE_BOMB =
            ENTITIES.register("sea_of_fire_bomb",
                    () -> EntityType.Builder.<SeaOfFireBomb>of(
                                    (EntityType.EntityFactory<SeaOfFireBomb>) (type, level) ->
                                            new SeaOfFireBomb((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.75f, 0.75f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "sea_of_fire_bomb").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<RaiseDanmakuProjectile>> RAISE_DANMAKU_PROJECTILE =
            ENTITIES.register("raise_danmaku_projectile",
                    () -> EntityType.Builder.<RaiseDanmakuProjectile>of(
                                    (EntityType.EntityFactory<RaiseDanmakuProjectile>) (type, level) ->
                                            new RaiseDanmakuProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "raise_danmaku_projectile").toString()));

    public static final RegistryObject<EntityType<FireFryingPanEntity>> FIRE_FRYING_PAN =
            ENTITIES.register("fire_frying_pan",
                    () -> EntityType.Builder.<FireFryingPanEntity>of(FireFryingPanEntity::new, MobCategory.MISC)
                            .sized(1.8f, 1.2f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "fire_frying_pan").toString()));

    public static final RegistryObject<EntityType<MagicImpactCrossEntity>> MAGIC_IMPACT_CROSS =
            ENTITIES.register("magic_impact_cross",
                    () -> EntityType.Builder.<MagicImpactCrossEntity>of(MagicImpactCrossEntity::new, MobCategory.MISC)
                            .sized(20.0f, 60.0f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "magic_impact_cross").toString()));

    public static final RegistryObject<EntityType<AscensionCrossEntity>> ASCENSION_CROSS =
            ENTITIES.register("ascension_cross",
                    () -> EntityType.Builder.<AscensionCrossEntity>of(AscensionCrossEntity::new, MobCategory.MISC)
                            .sized(1.5f, 4.0f)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "ascension_cross").toString()));

    public static final RegistryObject<EntityType<CrimsonYoungMoonEntity>> CRIMSON_YOUNG_MOON =
            ENTITIES.register("crimson_young_moon",
                    () -> EntityType.Builder.<CrimsonYoungMoonEntity>of(CrimsonYoungMoonEntity::new, MobCategory.MISC)
                            .sized(12.0f, 12.0f)
                            .clientTrackingRange(512)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "crimson_young_moon").toString()));

    public static final RegistryObject<EntityType<MeshOfLightAndDarknessControllerEntity>> MESH_OF_LIGHT_AND_DARKNESS_CONTROLLER =
            ENTITIES.register("mesh_of_light_and_darkness_controller",
                    () -> EntityType.Builder.<MeshOfLightAndDarknessControllerEntity>of(
                                    MeshOfLightAndDarknessControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "mesh_of_light_and_darkness_controller").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<YukariLaserDanmakuProjectile>> YUKARI_LASER_DANMAKU_PROJECTILE =
            ENTITIES.register("yukari_laser_danmaku_projectile",
                    () -> EntityType.Builder.<YukariLaserDanmakuProjectile>of(
                                    (EntityType.EntityFactory<YukariLaserDanmakuProjectile>) (type, level) ->
                                            new YukariLaserDanmakuProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "yukari_laser_danmaku_projectile").toString()));

    public static final RegistryObject<EntityType<MeshOfLightAndDarknessLaserEntity>> MESH_OF_LIGHT_AND_DARKNESS_LASER =
            ENTITIES.register("mesh_of_light_and_darkness_laser",
                    () -> EntityType.Builder.<MeshOfLightAndDarknessLaserEntity>of(
                                    MeshOfLightAndDarknessLaserEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "mesh_of_light_and_darkness_laser").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<TouhouPresetBulletProjectile>> TOUHOU_PRESET_BULLET =
            ENTITIES.register("touhou_preset_bullet",
                    () -> EntityType.Builder.<TouhouPresetBulletProjectile>of(
                                    (EntityType.EntityFactory<TouhouPresetBulletProjectile>) (type, level) ->
                                            new TouhouPresetBulletProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.8f, 0.8f)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "touhou_preset_bullet").toString()));

    public static final RegistryObject<EntityType<TouhouLaserEntity>> TOUHOU_LASER =
            ENTITIES.register("touhou_laser",
                    () -> EntityType.Builder.<TouhouLaserEntity>of(TouhouLaserEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "touhou_laser").toString()));

    public static final RegistryObject<EntityType<InnateDreamControllerEntity>> INNATE_DREAM_CONTROLLER =
            ENTITIES.register("innate_dream_controller",
                    () -> EntityType.Builder.<InnateDreamControllerEntity>of(InnateDreamControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "innate_dream_controller").toString()));

    public static final RegistryObject<EntityType<MasterSparkControllerEntity>> MASTER_SPARK_CONTROLLER =
            ENTITIES.register("master_spark_controller",
                    () -> EntityType.Builder.<MasterSparkControllerEntity>of(MasterSparkControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "master_spark_controller").toString()));

    public static final RegistryObject<EntityType<InheritedRitualControllerEntity>> INHERITED_RITUAL_CONTROLLER =
            ENTITIES.register("inherited_ritual_controller",
                    () -> EntityType.Builder.<InheritedRitualControllerEntity>of(InheritedRitualControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "inherited_ritual_controller").toString()));

    public static final RegistryObject<EntityType<MidnightChorusMasterControllerEntity>> MIDNIGHT_CHORUS_MASTER_CONTROLLER =
            ENTITIES.register("midnight_chorus_master_controller",
                    () -> EntityType.Builder.<MidnightChorusMasterControllerEntity>of(MidnightChorusMasterControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "midnight_chorus_master_controller").toString()));

    public static final RegistryObject<EntityType<YoukaiPolygraphControllerEntity>> YOUKAI_POLYGRAPH_CONTROLLER =
            ENTITIES.register("youkai_polygraph_controller",
                    () -> EntityType.Builder.<YoukaiPolygraphControllerEntity>of(YoukaiPolygraphControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "youkai_polygraph_controller").toString()));

    public static final RegistryObject<EntityType<DoubleBlackDeathButterflyControllerEntity>> DOUBLE_BLACK_DEATH_BUTTERFLY_CONTROLLER =
            ENTITIES.register("double_black_death_butterfly_controller",
                    () -> EntityType.Builder.<DoubleBlackDeathButterflyControllerEntity>of(DoubleBlackDeathButterflyControllerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "double_black_death_butterfly_controller").toString()));

    public static final RegistryObject<EntityType<ScarletMeisterSweepEntity>> SCARLET_MEISTER_SWEEP =
            ENTITIES.register("scarlet_meister_sweep",
                    () -> EntityType.Builder.<ScarletMeisterSweepEntity>of(ScarletMeisterSweepEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "scarlet_meister_sweep").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<ScarletMeisterProjectile>> SCARLET_MEISTER_PROJECTILE =
            ENTITIES.register("scarlet_meister_projectile",
                    () -> EntityType.Builder.<ScarletMeisterProjectile>of(
                                    (EntityType.EntityFactory<ScarletMeisterProjectile>) (type, level) ->
                                            new ScarletMeisterProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "scarlet_meister_projectile").toString()));

    public static final RegistryObject<EntityType<PoisonSplashStormFieldEntity>> POISON_SPLASH_STORM_FIELD =
            ENTITIES.register("poison_splash_storm_field",
                    () -> EntityType.Builder.<PoisonSplashStormFieldEntity>of(PoisonSplashStormFieldEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "poison_splash_storm_field").toString()));

    public static final RegistryObject<EntityType<DanmakuMeisterSweepEntity>> DANMAKU_MEISTER_SWEEP =
            ENTITIES.register("danmaku_meister_sweep",
                    () -> EntityType.Builder.<DanmakuMeisterSweepEntity>of(DanmakuMeisterSweepEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_meister_sweep").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<DanmakuMeisterProjectile>> DANMAKU_MEISTER_PROJECTILE =
            ENTITIES.register("danmaku_meister_projectile",
                    () -> EntityType.Builder.<DanmakuMeisterProjectile>of(
                                    (EntityType.EntityFactory<DanmakuMeisterProjectile>) (type, level) ->
                                            new DanmakuMeisterProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "danmaku_meister_projectile").toString()));
    public static final RegistryObject<EntityType<TimeStopFieldEntity>> TIME_STOP_FIELD =
            ENTITIES.register("time_stop_field",
                    () -> EntityType.Builder.<TimeStopFieldEntity>of(TimeStopFieldEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(256)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "time_stop_field").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<FireFryingPanProjectile>> FIRE_FRYING_PAN_PROJECTILE =
            ENTITIES.register("fire_frying_pan_projectile",
                    () -> EntityType.Builder.<FireFryingPanProjectile>of(
                                    (EntityType.EntityFactory<FireFryingPanProjectile>) (type, level) ->
                                            new FireFryingPanProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "fire_frying_pan_projectile").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<CrimsonYoungMoonProjectile>> CRIMSON_YOUNG_MOON_PROJECTILE =
            ENTITIES.register("crimson_young_moon_projectile",
                    () -> EntityType.Builder.<CrimsonYoungMoonProjectile>of(
                                    (EntityType.EntityFactory<CrimsonYoungMoonProjectile>) (type, level) ->
                                            new CrimsonYoungMoonProjectile((EntityType<? extends Projectile>) type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "crimson_young_moon_projectile").toString()));

    public static final RegistryObject<EntityType<SpinningSwordEntity>> SPINNING_SWORD =
            ENTITIES.register("spinning_sword",
                    () -> EntityType.Builder.<SpinningSwordEntity>of(SpinningSwordEntity::new, MobCategory.MISC)
                            .sized(0.5f, 1.0f)
                            .clientTrackingRange(10)
                            .updateInterval(3)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "spinning_sword").toString()));

    public static final RegistryObject<EntityType<TridentBombProjectile>> TRIDENT_BOMB =
            ENTITIES.register("trident_bomb",
                    () -> EntityType.Builder.<TridentBombProjectile>of(TridentBombProjectile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "trident_bomb").toString()));

    public static final RegistryObject<EntityType<TridentShardEntity>> TRIDENT_SHARD =
            ENTITIES.register("trident_shard",
                    () -> EntityType.Builder.<TridentShardEntity>of(TridentShardEntity::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "trident_shard").toString()));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<ArrowStormArrow>> ARROW_STORM_ARROW =
            ENTITIES.register("arrow_storm_arrow",
                    () -> EntityType.Builder.<ArrowStormArrow>of(
                                    (EntityType.EntityFactory<ArrowStormArrow>) (type, level) ->
                                            new ArrowStormArrow(type, level),
                                    MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(10)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "arrow_storm_arrow").toString()));

    public static final RegistryObject<EntityType<TrueArrowVolleyEntity>> TRUE_ARROW_VOLLEY =
            ENTITIES.register("true_arrow_volley",
                    () -> EntityType.Builder.<TrueArrowVolleyEntity>of(TrueArrowVolleyEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(3)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "true_arrow_volley").toString()));

    public static final RegistryObject<EntityType<HyperBlackHoleEntity>> HYPER_BLACK_HOLE =
            ENTITIES.register("hyper_black_hole",
                    () -> EntityType.Builder.<HyperBlackHoleEntity>of(HyperBlackHoleEntity::new, MobCategory.MISC)
                            .sized(60f, 60f)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "hyper_black_hole").toString()));

    public static final RegistryObject<EntityType<HyperBlackHoleMissile>> HYPER_BLACK_HOLE_MISSILE =
            ENTITIES.register("hyper_black_hole_missile",
                    () -> EntityType.Builder.<HyperBlackHoleMissile>of(HyperBlackHoleMissile::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "hyper_black_hole_missile").toString()));

    /** 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｩ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｦ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ貊捺ｱ壹・・ｽ繝ｻ・ｱ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬯ｮ・ｯ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ・ｷ繝ｻ・ｿ鬯ｮ・｢・つ郢晢ｽｻ繝ｻ・ｾ髯ｷ莨夲ｽｽ・ｱ驛｢譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｣鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬯ｮ・ｯ繝ｻ・ｷ郢晢ｽｻ繝ｻ・ｿ鬯ｯ・ｮ繝ｻ・｢繝ｻ縺､ﾂ鬮ｫ・ｴ鬲・ｼ夲ｽｽ・ｽ繝ｻ・ｭ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｸ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｮ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｣鬯ｮ・ｯ陷茨ｽｷ繝ｻ・ｽ繝ｻ・ｹ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｶ鬯ｮ・ｫ繝ｻ・ｴ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｱ鬯ｮ・ｯ隶厄ｽｸ繝ｻ・ｽ繝ｻ・ｳ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｦ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｮ繝ｻ・ｯ髫ｲ蟶吶・繝ｻ・ｻ繝ｻ・ｸ鬮ｴ蜿厄ｽｻ鬥ｴ・ｱ蝣､・ｹ譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｬ鬯ｯ・ｮ繝ｻ・ｮ髯ｷ闌ｨ・ｽ・ｷ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｶ鬯ｯ・ｯ繝ｻ・ｩ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｵ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｣鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｼ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｫ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ・ｷ繝ｻ・ｿ鬯ｮ・｢・つ郢晢ｽｻ繝ｻ・ｾ髯ｷ莨夲ｽｽ・ｱ驛｢譎｢・ｽ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｯ鬮ｯ蛹ｺ・ｻ繧托ｽｽ・ｽ繝ｻ・ｩ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｲ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ*/
    public static final RegistryObject<EntityType<ClownpieceFieldEntity>> CLOWNPIECE_FIELD =
            ENTITIES.register("clownpiece_field",
                    () -> EntityType.Builder.<ClownpieceFieldEntity>of(ClownpieceFieldEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(8)
                            .updateInterval(2)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "clownpiece_field").toString()));

    /** 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｩ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｦ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ貊捺ｱ壹・・ｽ繝ｻ・ｱ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬯ｮ・ｯ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｮ・ｮ驕擾ｽｩ繝ｻ・ｰ鬩｢蟠趣ｽ｢荵滂ｽｹ譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬮ｯ讖ｸ・ｽ・｢郢晢ｽｻ繝ｻ・ｹ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｷ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｯ隲幄ご陲也ｹ晢ｽｻ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｱ鬮ｯ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・､驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｬ鬮ｯ・ｷ鬮ｮ竏夲ｼ繝ｻ縺､ﾂ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ*/
    public static final RegistryObject<EntityType<ClownpieceBulletEntity>> CLOWNPIECE_BULLET =
            ENTITIES.register("clownpiece_bullet",
                    () -> EntityType.Builder.<ClownpieceBulletEntity>of(ClownpieceBulletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "clownpiece_bullet").toString()));

    /** 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｩ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｦ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ貊捺ｱ壹・・ｽ繝ｻ・ｱ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬯ｮ・ｯ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・､鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｬ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｼ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｶ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｼ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ*/
    public static final RegistryObject<EntityType<ClownpieceLaserEntity>> CLOWNPIECE_LASER =
            ENTITIES.register("clownpiece_laser",
                    () -> EntityType.Builder.<ClownpieceLaserEntity>of(ClownpieceLaserEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "clownpiece_laser").toString()));

    /** 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｣鬯ｮ・ｯ繝ｻ・ｷ郢晢ｽｻ繝ｻ・ｴ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｿ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬯ｮ・ｮ驕擾ｽｩ繝ｻ・｡陋溘・・ｽ・ｾ繝ｻ・ｭ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｲ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｶ鬮ｫ・ｰ陞滂ｽｲ繝ｻ・ｽ繝ｻ・ｵ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｭ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｯ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｩ鬯ｮ・ｫ繝ｻ・ｰ郢晢ｽｻ繝ｻ・ｳ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｱ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｯ鬮ｫ・ｶ陷ｴ繝ｻ・ｽ・ｽ繝ｻ・ｸ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｩ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｷ鬯ｯ・ｮ繝ｻ・｣鬮ｮ蜈ｷ・ｽ・ｻ郢晢ｽｻ繝ｻ・｣郢晢ｽｻ繝ｻ・ｰ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・･鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｯ繝ｻ・ｩ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｮ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｯ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｷ鬯ｯ・ｮ繝ｻ・ｯ髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｯ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｩ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｭ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬯ｯ・ｮ繝ｻ・ｦ郢晢ｽｻ繝ｻ・ｪ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｲ鬯ｯ・ｯ繝ｻ・ｮ郢晢ｽｻ繝ｻ・ｫ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｶ鬯ｮ・ｫ繝ｻ・ｶ髯ｷ・ｷ繝ｻ・ｩ郢晢ｽｻ繝ｻ・ｸ郢晢ｽｻ繝ｻ・ｻ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｰ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ*/
    public static final RegistryObject<EntityType<ApostleBeamCrossEntity>> APOSTLE_BEAM_CROSS =
            ENTITIES.register("apostle_beam_cross",
                    () -> EntityType.Builder.<ApostleBeamCrossEntity>of(ApostleBeamCrossEntity::new, MobCategory.MISC)
                            .sized(15.0f, 30.0f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "apostle_beam_cross").toString()));

    /** 鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ譏ｴ繝ｻ繝ｻ閧ｲ・ｹ譎｢・ｽ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｭ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｮ繝ｻ・ｯ髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｶ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｷ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ貊捺ｱ壹・・ｽ繝ｻ・ｱ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ驛｢・ｧ隰・∞・ｽ・ｽ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｧ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬯ｮ・｣陋ｹ繝ｻ・ｽ・ｽ繝ｻ・ｳ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｸ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｷ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮｣蛹・ｽｽ・ｳ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ鬯ｮ・ｫ繝ｻ・ｶ髫ｰ謦ｰ・ｽ・ｺ郢晢ｽｻ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ鬩募争豎壹・・ｽ繝ｻ・ｭ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｯ譏ｴ繝ｻ繝ｻ閧ｲ・ｹ譎｢・ｽ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｾ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｭ鬯ｯ・ｯ繝ｻ・ｩ髯晢ｽｷ繝ｻ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬯ｮ・ｫ繝ｻ・ｴ鬮ｮ諛ｶ・ｽ・｣郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・｢鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｣鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｰ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｧ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｨ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬯ｮ・ｮ隲幢ｽｶ繝ｻ・ｽ繝ｻ・｣驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｳ鬯ｯ・ｯ繝ｻ・ｯ郢晢ｽｻ繝ｻ・ｩ鬮ｯ譎｢・ｽ・ｷ郢晢ｽｻ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・｢鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｩ蟷｢・ｽ・｢髫ｴ雜｣・ｽ・｢郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｻ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｹ鬯ｯ・ｮ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｴ鬮ｫ・ｰ繝ｻ・ｫ郢晢ｽｻ繝ｻ・ｾ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｴ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ鬯ｯ・ｯ繝ｻ・ｩ髫ｰ・ｳ繝ｻ・ｾ郢晢ｽｻ繝ｻ・ｽ郢晢ｽｻ繝ｻ・ｵ鬩幢ｽ｢隴趣ｽ｢繝ｻ・ｽ繝ｻ・ｻ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｺ鬯ｯ・ｩ陝ｷ・｢繝ｻ・ｽ繝ｻ・｢鬮ｫ・ｴ髮懶ｽ｣繝ｻ・ｽ繝ｻ・｢驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｽ驛｢譎｢・ｽ・ｻ郢晢ｽｻ繝ｻ・ｻ*/
    public static final RegistryObject<EntityType<AbyssBlastEntity>> ABYSS_BLAST =
            ENTITIES.register("abyss_blast",
                    () -> EntityType.Builder.<AbyssBlastEntity>of(AbyssBlastEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(256)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "abyss_blast").toString()));
    public static final RegistryObject<EntityType<ShoeEntity>> SHOE =
            ENTITIES.register("shoe",
                    () -> EntityType.Builder.<ShoeEntity>of(ShoeEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(12)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "shoe").toString()));

    public static final RegistryObject<EntityType<ResourceGlowMarkerEntity>> RESOURCE_GLOW_MARKER =
            ENTITIES.register("resource_glow_marker",
                    () -> EntityType.Builder.<ResourceGlowMarkerEntity>of(ResourceGlowMarkerEntity::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(64)
                            .updateInterval(20)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "resource_glow_marker").toString()));
    public static final RegistryObject<EntityType<MassBreakHammerEntity>> MASS_BREAK_HAMMER =
            ENTITIES.register("mass_break_hammer_logic",
                    () -> EntityType.Builder.<MassBreakHammerEntity>of(MassBreakHammerEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "mass_break_hammer_logic").toString()));
    public static final RegistryObject<EntityType<HeartOrbitEntity>> HEART_ORBIT =
            ENTITIES.register("heart_orbit",
                    () -> EntityType.Builder.<HeartOrbitEntity>of(HeartOrbitEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "heart_orbit").toString()));
    public static final RegistryObject<EntityType<SlimeBlockProjectile>> SLIME_BLOCK_PROJECTILE =
            ENTITIES.register("slime_block_projectile",
                    () -> EntityType.Builder.<SlimeBlockProjectile>of(SlimeBlockProjectile::new, MobCategory.MISC)
                            .sized(1.0f, 1.0f)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "slime_block_projectile").toString()));

    public static final RegistryObject<EntityType<HealingCrystalEntity>> HEALING_CRYSTAL =
            ENTITIES.register("healing_crystal",
                    () -> EntityType.Builder.<HealingCrystalEntity>of(HealingCrystalEntity::new, MobCategory.MISC)
                            .sized(1.0f, 2.0f)
                            .clientTrackingRange(32)
                            .updateInterval(5)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "healing_crystal").toString()));
    public static final RegistryObject<EntityType<YinYangBallEntity>> YIN_YANG_BALL =
            ENTITIES.register("yin_yang_ball",
                    () -> EntityType.Builder.<YinYangBallEntity>of(YinYangBallEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(16)
                            .updateInterval(2)
                            .build(new ResourceLocation(ElementMagicBossMod.MOD_ID, "yin_yang_ball").toString()));






    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}

