package com.ecrea.elementmagicboss.entity;

import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.ExtendedServerBossEvent;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.sounds.SoundSource;
import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.client.util.BossbarManager;

import java.util.List;
import java.util.UUID;

/**
 * Angel Raziel Entity
 * 天使ラジエル
 */
public class AngelRazielEntity extends AbstractSpellCastingMob implements GeoEntity, IClientEventEntity {
    public static final byte CLIENT_STOP_TRACKING = 0;
    public static final byte CLIENT_START_TRACKING = 1;

    private static final BossbarManager.BossbarSprite BOSSBAR_SPRITE = new BossbarManager.BossbarSprite(
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/angel_raziel_empty.png"),
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/angel_raziel_full.png"),
        256, 64, 0, 20, 256, 64);

    @Override
    public void handleClientEvent(byte eventId) {
        switch (eventId) {
            case CLIENT_STOP_TRACKING -> {
                if (this.bossEvent != null) {
                    BossbarManager.stopTracking(this.bossEvent.getId());
                }
            }
            case CLIENT_START_TRACKING -> {
                if (this.bossEvent != null) {
                    BossbarManager.startTracking(this.bossEvent.getId(), BOSSBAR_SPRITE);
                }
            }
        }
    }

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(AngelRazielEntity.class, EntityDataSerializers.INT);
    private ExtendedServerBossEvent bossEvent;
    private int musicTick = 0;
    private static final int MUSIC_LOOP_1_TICKS = 4100; // 3:25 = 205s = 4100 ticks
    private static final int MUSIC_LOOP_2_TICKS = 4200; // 3:30 = 210s = 4200 ticks
    
    // フェーズ定義
    public static final int PHASE_FLYING = 1;
    public static final int PHASE_GROUNDED = 2;
    public static final int PHASE_TRANSITIONING = 3;

    private int transitionTimer = 0;
    private final FlyingMoveControl flyingMoveControl;
    private final net.minecraft.world.entity.ai.control.MoveControl groundMoveControl;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public AngelRazielEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 1000;
        createBossEvent();
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        this.groundMoveControl = new net.minecraft.world.entity.ai.control.MoveControl(this);
    }

    protected void createBossEvent() {
        this.bossEvent = (ExtendedServerBossEvent) new ExtendedServerBossEvent(
            this.getUUID(),
            Component.translatable("entity.elementmagicboss.angel_raziel"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
        );
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, PHASE_FLYING);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.initEquipment();
    }

    private void initEquipment() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(Enchantments.SHARPNESS, 20);
        sword.enchant(Enchantments.MOB_LOOTING, 20);
        sword.enchant(Enchantments.FIRE_ASPECT, 20);
        
        this.setItemSlot(EquipmentSlot.MAINHAND, sword);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F); 
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        this.goalSelector.addGoal(1, new com.ecrea.elementmagicboss.entity.ai.AngelRazielFlyGoal(this));
        
        this.goalSelector.addGoal(2, new com.ecrea.elementmagicboss.entity.ai.AngelRazielAttackGoal(
            this, 1.0D, 3, 10
        ));
        
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 10.0D)
            .add(Attributes.FOLLOW_RANGE, 64.0D)
            .add(Attributes.ARMOR, 30.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
            .add(Attributes.FLYING_SPEED, 0.4D)
            .add(AttributeRegistry.MAX_MANA.get(), 100000.0D)
            .add(AttributeRegistry.COOLDOWN_REDUCTION.get(), 100.0D)
            .add(AttributeRegistry.CAST_TIME_REDUCTION.get(), 1.0D);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        SpawnGroupData data = super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
        this.initEquipment();
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!this.level().isClientSide) {
            musicTick++;
            int currentMax = (this.getPhase() == PHASE_FLYING) ? MUSIC_LOOP_1_TICKS : MUSIC_LOOP_2_TICKS;
            if (musicTick >= currentMax) {
                musicTick = 0;
                triggerMusicToAll(false);
            }
        }

        int currentPhase = this.getPhase();

        if (currentPhase == PHASE_FLYING) {
            this.setNoGravity(true);
            if (!this.level().isClientSide) {
                boolean isCloseToGround = Utils.raycastForBlock(this.level(), this.position(), this.position().subtract(0, 2.5, 0), ClipContext.Fluid.ANY).getType() == HitResult.Type.BLOCK;
                
                Vec3 woosh = new Vec3(
                    Mth.sin((this.tickCount * 5) * Mth.DEG_TO_RAD),
                    (Mth.cos((this.tickCount * 3 + 986741) * Mth.DEG_TO_RAD) + (isCloseToGround ? .05 : -.185)) * .5f,
                    Mth.sin((this.tickCount * 1 + 465) * Mth.DEG_TO_RAD)
                );
                
                if (this.getTarget() == null) {
                    woosh = woosh.scale(0.25f);
                }
                this.setDeltaMovement(this.getDeltaMovement().add(woosh.scale(0.0085f)));
                
                if (this.isAggressive() && this.getTarget() != null && this.distanceToSqr(this.getTarget()) > 16.0D) {
                    this.setDeltaMovement(this.getDeltaMovement().add(this.getForward().scale(0.02)));
                }

                this.moveControl = this.flyingMoveControl;
                if (!(this.navigation instanceof FlyingPathNavigation)) {
                    this.navigation = new FlyingPathNavigation(this, this.level());
                }
            } else {
                if (!this.isInvisible()) {
                    float radius = 0.5f;
                    for (int i = 0; i < 3; i++) {
                        this.level().addParticle(ParticleTypes.END_ROD, this.getRandomX(radius), this.getRandomY(), this.getRandomZ(radius), 0, -0.05, 0);
                    }
                }
            }
        } else if (currentPhase == PHASE_TRANSITIONING) {
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8D).add(0, 0.02, 0)); // ゆっくり上昇
            
            if (!this.level().isClientSide) {
                this.transitionTimer--;
                if (this.transitionTimer <= 0) {
                    this.setPhase(PHASE_GROUNDED);
                    this.setHealth(this.getMaxHealth()); // 体力全回復
                    this.setInvulnerable(false);
                    this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 1.0F);
                    MagicManager.spawnParticles(this.level(), ParticleHelper.FIRE, this.getX(), this.getY() + 1.5, this.getZ(), 100, 0.3, 0.3, 0.3, 0.5, true);
                }
            } else {
                // 移行中の爆発的パーティクル
                if (this.getTarget() != null) {
            double d0 = this.getTarget().getX() - this.getX();
            double d1 = this.getTarget().getY(0.5D) - this.getY(0.5D);
            double d2 = this.getTarget().getZ() - this.getZ();
            
            if (this.level() instanceof ServerLevel serverLevel) {
                MagicManager.spawnParticles(serverLevel, ParticleHelper.ELECTRICITY, this.getX(), this.getY(), this.getZ(), 10, 0.5D, 0.5D, 0.5D, 0.1D, false);
            }
                }
            }
        } else {
            // 第2フェーズ：地上
            this.setNoGravity(false);
            if (!this.level().isClientSide) {
                this.moveControl = this.groundMoveControl;
                if (!(this.navigation instanceof GroundPathNavigation)) {
                    this.navigation = new GroundPathNavigation(this, this.level());
                }
            }
        }
    }

    @Override
    public boolean bobBodyWhileWalking() {
        return this.getPhase() == PHASE_GROUNDED;
    }

    @Override
    public boolean shouldAlwaysAnimateLegs() {
        return this.getPhase() == PHASE_GROUNDED;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    public void die(DamageSource pDamageSource) {
        // LivingHurtEvent でダメージが増幅されてhurt()チェックをすり抜けた場合の安全網
        if (!this.level().isClientSide && this.getPhase() == PHASE_FLYING) {
            this.setHealth(10.0F);
            this.setPhase(PHASE_TRANSITIONING);
            this.transitionTimer = 80;
            this.setInvulnerable(true);
            this.cancelCast();
            this.playSound(SoundEvents.WITHER_DEATH, 1.0F, 1.0F);
            this.musicTick = 0;
            triggerMusicToAll(true);
            return; // 死亡させない
        }
        super.die(pDamageSource);
        
        if (!this.level().isClientSide) {
            List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D));
            for (ServerPlayer player : players) {
                stopAllBossMusic(player);
            }
            int xp = 1000;
            while (xp > 0) {
                int drop = ExperienceOrb.getExperienceValue(xp);
                xp -= drop;
                this.level().addFreshEntity(
                    new ExperienceOrb(
                        this.level(), this.getX(), this.getY(), this.getZ(), drop
                    )
                );
            }
            
            // 天使の指輪のドロップ
            this.spawnAtLocation(com.ecrea.elementmagicboss.item.ModItems.ANGEL_RING.get());
        }
    }

    private void triggerMusicToAll(boolean stopPrevious) {
        for (ServerPlayer player : this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64))) {
            if (stopPrevious) stopAllBossMusic(player);
            playCurrentMusicTo(player);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
        this.serverTriggerEvent(CLIENT_START_TRACKING);
        playCurrentMusicTo(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
        this.serverTriggerEvent(CLIENT_STOP_TRACKING);
        stopAllBossMusic(player);
    }

    private void playCurrentMusicTo(ServerPlayer player) {
        SoundEvent sound = (this.getPhase() == PHASE_FLYING) ? 
            com.ecrea.elementmagicboss.sound.ModSounds.RAZIEL_BATTLE_MUSIC_1.get() : 
            com.ecrea.elementmagicboss.sound.ModSounds.RAZIEL_BATTLE_MUSIC_2.get();
        
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
            net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(sound), 
            net.minecraft.sounds.SoundSource.MUSIC, 
            player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, player.getRandom().nextLong()));
    }

    private void stopAllBossMusic(ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
            com.ecrea.elementmagicboss.sound.ModSounds.RAZIEL_BATTLE_MUSIC_1.getId(), net.minecraft.sounds.SoundSource.MUSIC));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
            com.ecrea.elementmagicboss.sound.ModSounds.RAZIEL_BATTLE_MUSIC_2.getId(), net.minecraft.sounds.SoundSource.MUSIC));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.initEquipment(); // 読み込み時にも確実に装備
        if (pCompound.contains("angel_phase")) {
            this.setPhase(pCompound.getInt("angel_phase"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("angel_phase", this.getPhase());
    }

    private final software.bernie.geckolib.core.animation.AnimationController<AngelRazielEntity> wingsController = 
        new software.bernie.geckolib.core.animation.AnimationController<>(this, "wings_controller", 0, this::wingsPredicate);

    private software.bernie.geckolib.core.object.PlayState wingsPredicate(software.bernie.geckolib.core.animation.AnimationState<AngelRazielEntity> event) {
        event.getController().setAnimation(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop("animation.wing_flap_fixed"));
        return software.bernie.geckolib.core.object.PlayState.CONTINUE;
    }

    private final software.bernie.geckolib.core.animation.AnimationController<AngelRazielEntity> transitionController = 
        new software.bernie.geckolib.core.animation.AnimationController<>(this, "transition_controller", 0, this::transitionPredicate);

    private software.bernie.geckolib.core.object.PlayState transitionPredicate(software.bernie.geckolib.core.animation.AnimationState<AngelRazielEntity> event) {
        if (this.getPhase() == PHASE_TRANSITIONING) {
            event.getController().setAnimation(software.bernie.geckolib.core.animation.RawAnimation.begin().thenPlay("transition_pose"));
            return software.bernie.geckolib.core.object.PlayState.CONTINUE;
        }
        return software.bernie.geckolib.core.object.PlayState.STOP;
    }

    @Override
    public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
        controllers.add(wingsController);
        controllers.add(transitionController);
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceSq) {
        return false;
    }

    @Override
    public void checkDespawn() {
        // ボスなので自然デスポーンさせない
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && this.getPhase() == PHASE_TRANSITIONING) {
            return false;
        }
        // 第1フェーズから第3フェーズ（移行）への突入チェック（致死ダメージまたは体力10以下）
        if (this.getPhase() == PHASE_FLYING && (this.getHealth() - amount <= 10.0F)) {
            this.setHealth(10.0F); // 体力を10でロック
            this.setPhase(PHASE_TRANSITIONING);
            this.transitionTimer = 80; // 4秒間の移行時間
            this.setInvulnerable(true);
            this.cancelCast();
            this.playSound(SoundEvents.WITHER_DEATH, 1.0F, 1.0F);
            this.musicTick = 0;
            triggerMusicToAll(true);
            return false;
        }
        
        return super.hurt(source, amount);
    }
}
