package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, ElementMagicBossMod.MOD_ID);

    public static final RegistryObject<AbstractSpell> STORM_BARRAGE =
            SPELLS.register("storm_barrage", StormBarrageSpell::new);
    public static final RegistryObject<AbstractSpell> DANMAKU_SHOT =
            SPELLS.register("danmaku_shot", DanmakuShotSpell::new);
    public static final RegistryObject<AbstractSpell> BLAZING_STAR =
            SPELLS.register("blazing_star", BlazingStarSpell::new);
    public static final RegistryObject<AbstractSpell> SUN_STORM =
            SPELLS.register("sun_storm", SunStormSpell::new);
    public static final RegistryObject<AbstractSpell> BLOOD_RAIN =
            SPELLS.register("blood_rain", BloodRainSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_BLAZING_STORM =
            SPELLS.register("true_blazing_storm", TrueBlazingStormSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_ARROW_VOLLEY =
            SPELLS.register("true_arrow_volley", TrueArrowVolleySpell::new);
    public static final RegistryObject<AbstractSpell> FLYING_BLOOD_SLASH =
            SPELLS.register("flying_blood_slash", FlyingBloodSlashSpell::new);
    public static final RegistryObject<AbstractSpell> SEA_OF_FIRE =
            SPELLS.register("sea_of_fire", SeaOfFireSpell::new);
    public static final RegistryObject<AbstractSpell> FIRE_FRYING_PAN =
            SPELLS.register("fire_frying_pan", FireFryingPanSpell::new);
    public static final RegistryObject<AbstractSpell> MAGIC_IMPACT =
            SPELLS.register("magic_impact", MagicImpactSpell::new);
    public static final RegistryObject<AbstractSpell> CRIMSON_YOUNG_MOON =
            SPELLS.register("crimson_young_moon", CrimsonYoungMoonSpell::new);
    public static final RegistryObject<AbstractSpell> MESH_OF_LIGHT_AND_DARKNESS =
            SPELLS.register("mesh_of_light_and_darkness", MeshOfLightAndDarknessSpell::new);
    public static final RegistryObject<AbstractSpell> SCARLET_MEISTER =
            SPELLS.register("scarlet_meister", ScarletMeisterSpell::new);
    public static final RegistryObject<AbstractSpell> ACID_STORM =
            SPELLS.register("acid_storm", AcidStormSpell::new);
    public static final RegistryObject<AbstractSpell> POISON_SPLASH_STORM =
            SPELLS.register("poison_splash_storm", PoisonSplashStormSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_STARFALL =
            SPELLS.register("true_starfall", TrueStarfallSpell::new);
    public static final RegistryObject<AbstractSpell> DANMAKU_MEISTER =
            SPELLS.register("danmaku_meister", DanmakuMeisterSpell::new);
    public static final RegistryObject<AbstractSpell> INNATE_DREAM =
            SPELLS.register("innate_dream", InnateDreamSpell::new);
    public static final RegistryObject<AbstractSpell> MASTER_SPARK =
            SPELLS.register("master_spark", MasterSparkSpell::new);
    public static final RegistryObject<AbstractSpell> INHERITED_RITUAL =
            SPELLS.register("inherited_ritual", InheritedRitualSpell::new);
    public static final RegistryObject<AbstractSpell> MIDNIGHT_CHORUS_MASTER =
            SPELLS.register("midnight_chorus_master", MidnightChorusMasterSpell::new);
    public static final RegistryObject<AbstractSpell> YOUKAI_POLYGRAPH =
            SPELLS.register("youkai_polygraph", YoukaiPolygraphSpell::new);
    public static final RegistryObject<AbstractSpell> DOUBLE_BLACK_DEATH_BUTTERFLY =
            SPELLS.register("double_black_death_butterfly", DoubleBlackDeathButterflySpell::new);
    public static final RegistryObject<AbstractSpell> THE_WORLD =
            SPELLS.register("the_world", TheWorldSpell::new);
    public static final RegistryObject<AbstractSpell> RAISE_DANMAKU =
            SPELLS.register("raise_danmaku", RaiseDanmakuSpell::new);
    public static final RegistryObject<AbstractSpell> HYAHHA_BAKKA =
            SPELLS.register("hyahha_bakka", HyahhaBakkaSpell::new);
    public static final RegistryObject<AbstractSpell> KEN_NO_MAI =
            SPELLS.register("ken_no_mai", KenNoMaiSpell::new);
    public static final RegistryObject<AbstractSpell> MULTIPLY =
            SPELLS.register("multiply", MultiplySpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_COUNTERSPELL =
            SPELLS.register("true_counterspell", TrueCounterspellSpell::new);
    public static final RegistryObject<AbstractSpell> DANMAKU_BREATH =
            SPELLS.register("danmaku_breath", DanmakuBreathSpell::new);
    public static final RegistryObject<AbstractSpell> MULTI_ELDRITCH_BLAST =
            SPELLS.register("multi_eldritch_blast", MultiEldritchBlastSpell::new);
    public static final RegistryObject<AbstractSpell> ENCHANT_SYNTHESIS =
            SPELLS.register("enchant_synthesis", EnchantSynthesisSpell::new);
    public static final RegistryObject<AbstractSpell> ENCHANT_SEPARATION =
            SPELLS.register("enchant_separation", EnchantSeparationSpell::new);
    public static final RegistryObject<AbstractSpell> TRIDENT_BOMB =
            SPELLS.register("trident_bomb", TridentBombSpell::new);
    public static final RegistryObject<AbstractSpell> FROST_BEAM =
            SPELLS.register("frost_beam", FrostBeamSpell::new);
    public static final RegistryObject<AbstractSpell> ARROW_STORM =
            SPELLS.register("arrow_storm", ArrowStormSpell::new);
    public static final RegistryObject<AbstractSpell> SLASH_BARRIER =
            SPELLS.register("slash_barrier", SlashBarrierSpell::new);
    public static final RegistryObject<AbstractSpell> HYPER_BLACK_HOLE =
            SPELLS.register("hyper_black_hole", HyperBlackHoleSpell::new);
    public static final RegistryObject<AbstractSpell> DAMAGE_MULTIPLY =
            SPELLS.register("damage_multiply", DamageMultiplySpell::new);
    public static final RegistryObject<AbstractSpell> FREEZE_MULTIPLY =
            SPELLS.register("freeze_multiply", FreezeMultiplySpell::new);
    public static final RegistryObject<AbstractSpell> EFFECT_MULTIPLY =
            SPELLS.register("effect_multiply", EffectMultiplySpell::new);
    public static final RegistryObject<AbstractSpell> SURE_HIT =
            SPELLS.register("sure_hit", SureHitSpell::new);
    public static final RegistryObject<AbstractSpell> ETERNAL_FIRE =
            SPELLS.register("eternal_fire", EternalFireSpell::new);
    public static final RegistryObject<AbstractSpell> GOOGOL_SLASH =
            SPELLS.register("googol_slash", GoogolSlashSpell::new);
    public static final RegistryObject<AbstractSpell> ACCUMULATING_POISON =
            SPELLS.register("accumulating_poison", AccumulatingPoisonSpell::new);
    public static final RegistryObject<AbstractSpell> DIVINE_BLESSING =
            SPELLS.register("divine_blessing", DivineBlessingSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_ROOT_BINDING =
            SPELLS.register("true_root_binding", TrueRootBindingSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_PREDATOR =
            SPELLS.register("true_predator", TruePredatorSpell::new);
    public static final RegistryObject<AbstractSpell> POISON_ARROW_STORM =
            SPELLS.register("poison_arrow_storm", PoisonArrowStormSpell::new);
    public static final RegistryObject<AbstractSpell> ICE_AFTERIMAGE =
            SPELLS.register("ice_afterimage", IceAfterimageSpell::new);
    public static final RegistryObject<AbstractSpell> LIMIT_BREAK =
            SPELLS.register("limit_break", LimitBreakSpell::new);
    public static final RegistryObject<AbstractSpell> DANMAKU_RAIN =
            SPELLS.register("danmaku_rain", DanmakuRainSpell::new);
    public static final RegistryObject<AbstractSpell> CLOWNPIECE =
            SPELLS.register("clownpiece", ClownpieceSpell::new);
    public static final RegistryObject<AbstractSpell> ULTIMATE_EATER =
            SPELLS.register("ultimate_eater", UltimateEaterSpell::new);
    public static final RegistryObject<AbstractSpell> CURSE_OF_BLEEDING =
            SPELLS.register("curse_of_bleeding", CurseOfBleedingSpell::new);
    public static final RegistryObject<AbstractSpell> APOSTLE_BEAM =
            SPELLS.register("apostle_beam", ApostleBeamSpell::new);
    public static final RegistryObject<AbstractSpell> ABYSS_BLAST =
            SPELLS.register("abyss_blast", AbyssBlastSpell::new);
    public static final RegistryObject<AbstractSpell> CHAIN_CHAIN_CREEPER =
            SPELLS.register("chain_chain_creeper", ChainChainCreeperSpell::new);
    public static final RegistryObject<AbstractSpell> TRUE_CHAIN_LIGHTNING =
            SPELLS.register("true_chain_lightning", TrueChainLightningSpell::new);
    public static final RegistryObject<AbstractSpell> INVINCIBILITY =
            SPELLS.register("invincibility", InvincibilitySpell::new);
    public static final RegistryObject<AbstractSpell> VOID_STRIKE =
            SPELLS.register("void_strike", VoidStrikeSpell::new);
    public static final RegistryObject<AbstractSpell> DRAGON_SKIN =
            SPELLS.register("dragon_skin", DragonSkinSpell::new);
    public static final RegistryObject<AbstractSpell> MASS_BREAK_HAMMER =
            SPELLS.register("mass_break_hammer", MassBreakHammerSpell::new);
    public static final RegistryObject<AbstractSpell> ANTI_HEAL =
            SPELLS.register("anti_heal", AntiHealSpell::new);
    public static final RegistryObject<AbstractSpell> RESOURCE_DETECTION =
            SPELLS.register("resource_detection", ResourceDetectionSpell::new);
    public static final RegistryObject<AbstractSpell> SHOES_OF_DETERMINATION =
            SPELLS.register("shoes_of_determination", ShoesOfDeterminationSpell::new);
    public static final RegistryObject<AbstractSpell> WEATHER_CONTROL =
            SPELLS.register("weather_control", WeatherControlSpell::new);
    public static final RegistryObject<AbstractSpell> SUN_REVERSAL =
            SPELLS.register("sun_reversal", SunReversalSpell::new);
    public static final RegistryObject<AbstractSpell> RARE_DROP_GUARANTEE =
            SPELLS.register("rare_drop_guarantee", RareDropGuaranteeSpell::new);
    public static final RegistryObject<AbstractSpell> BREEDING_BLESSING =
            SPELLS.register("breeding_blessing", BreedingBlessingSpell::new);
    public static final RegistryObject<AbstractSpell> HEALING_DETERMINATION =
            SPELLS.register("healing_determination", HealingDeterminationSpell::new);
    public static final RegistryObject<AbstractSpell> SLIME_BLOCK_SPELL =
            SPELLS.register("slime_block_spell", SlimeBlockSpell::new);
    public static final RegistryObject<AbstractSpell> BLOOM =
            SPELLS.register("bloom", BloomSpell::new);
    public static final RegistryObject<AbstractSpell> CRYSTAL_HEAL =
            SPELLS.register("crystal_heal", CrystalHealSpell::new);
    public static final RegistryObject<AbstractSpell> FANTASY_SEAL =
            SPELLS.register("fantasy_seal", FantasySealSpell::new);
    public static final RegistryObject<AbstractSpell> TORCH_PLACE =
            SPELLS.register("torch_place", TorchPlaceSpell::new);
    public static final RegistryObject<AbstractSpell> GLOWSTONE_PLACE =
            SPELLS.register("glowstone_place", GlowstonePlaceSpell::new);

    private ModSpells() {}

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
