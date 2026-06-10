package com.ecrea.elementmagicboss.entity;

import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.ExtendedServerBossEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.MobType;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.client.util.BossbarManager;
import com.ecrea.elementmagicboss.sound.ModSounds;

import java.util.List;
import java.util.UUID;

public class SkeletonKingEntity extends AbstractSpellCastingMob implements GeoEntity, IClientEventEntity {
    public static final byte CLIENT_STOP_TRACKING = 0;
    public static final byte CLIENT_START_TRACKING = 1;

    private static final BossbarManager.BossbarSprite BOSSBAR_SPRITE = new BossbarManager.BossbarSprite(
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/skeleton_bar_empty.png"),
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/skeleton_bar_full.png"),
        256, 26, 0, 0, 256, 26);

    @Override
    public void handleClientEvent(byte eventId) {
        switch (eventId) {
            case CLIENT_STOP_TRACKING -> {
                BossbarManager.stopTracking(this.getUUID());
            }
            case CLIENT_START_TRACKING -> {
                BossbarManager.startTracking(this.getUUID(), BOSSBAR_SPRITE);
            }
        }
    }

    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(SkeletonKingEntity.class, EntityDataSerializers.INT);
    private ExtendedServerBossEvent bossEvent;
    
    private boolean judged = false;
    private int dialogueTicks = 0;
    private List<String> currentDialogue = null;
    private int dialogueIndex = 0;
    private Player judgementTarget = null;

    public SkeletonKingEntity(EntityType<? extends AbstractSpellCastingMob> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5000;
        createBossEvent();
    }

    protected void createBossEvent() {
        this.bossEvent = (ExtendedServerBossEvent) new ExtendedServerBossEvent(
            this.getUUID(),
            Component.translatable("entity.elementmagicboss.skeleton_king.phase", 1), 
            BossEvent.BossBarColor.WHITE, 
            BossEvent.BossBarOverlay.NOTCHED_6
        );
        this.bossEvent.setVisible(true);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 1);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    private int musicTickTimer = 0;
    private int currentPlayingPhase = 0;

    public void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
        // Boss bar name update is no longer needed for server boss bar as it will be hidden, 
        // but keeping it for consistency or fallbacks won't hurt.
        this.bossEvent.setName(Component.translatable("entity.elementmagicboss.skeleton_king.phase", phase));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(AttributeRegistry.MAX_MANA.get(), 1000000.0D)
                .add(AttributeRegistry.COOLDOWN_REDUCTION.get(), 100.0D)
                .add(AttributeRegistry.CAST_TIME_REDUCTION.get(), 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Judgement Dialogue Logic is handled in tick()
        
        this.goalSelector.addGoal(1, new com.ecrea.elementmagicboss.entity.ai.SkeletonKingAttackGoal(this, 1.25D, 20, 60));
        
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void tick() {
        if (currentDialogue != null) {
            // Dialogue processing
            handleDialogue();
            return; // Freeze movement/AI
        }

        super.tick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!this.level().isClientSide) {
            tickMusic();
            if (!judged) {
                Player player = this.level().getNearestPlayer(this, 10.0D);
                if (player instanceof ServerPlayer serverPlayer) {
                    startJudgement(serverPlayer);
                }
            }
        }
    }

    private void tickMusic() {
        if (musicTickTimer > 0) {
            musicTickTimer--;
        }

        int phase = this.getPhase();
        // Check for phase transitions that require music change
        // Phase 1-3: Music 1
        // Phase 4-5: Music 2
        // Phase 6: Music 3
        
        int nextMusicType = 0;
        if (phase <= 3) nextMusicType = 1;
        else if (phase <= 5) nextMusicType = 2;
        else nextMusicType = 3;

        boolean musicChanged = currentPlayingPhase != nextMusicType;

        if (musicTickTimer == 0 || musicChanged) {
            playPhaseMusic(nextMusicType);
            currentPlayingPhase = nextMusicType;
        }
    }

    private void playPhaseMusic(int musicType) {
        net.minecraft.sounds.SoundEvent musicToPlay = null;
        int duration = 0;

        if (musicType == 1) {
            musicToPlay = ModSounds.SKELETON_KING_MUSIC_1.get();
            duration = 242 * 20; // 4:02 = 242s
        } else if (musicType == 2) {
            musicToPlay = ModSounds.SKELETON_KING_MUSIC_2.get();
            duration = 202 * 20; // 3:22 = 202s
        } else if (musicType == 3) {
            musicToPlay = ModSounds.SKELETON_KING_MUSIC_3.get();
            duration = 267 * 20; // 4:27 = 267s
        }

        if (musicToPlay != null) {
            ClientboundStopSoundPacket stopMusic = 
                new ClientboundStopSoundPacket(null, SoundSource.MUSIC);
            ClientboundStopSoundPacket stopHostile = 
                new ClientboundStopSoundPacket(null, SoundSource.HOSTILE);
            
            this.bossEvent.getPlayers().forEach(player -> {
                 player.connection.send(stopMusic);
                 player.connection.send(stopHostile);
            });
            
            // Play new music as MUSIC category using packet to ensure category
            ClientboundSoundPacket playPacket = 
                new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(musicToPlay),
                    SoundSource.MUSIC,
                    this.getX(), this.getY(), this.getZ(),
                    3.4028235E38F, 1.0F, // Max volume (infinite range effectively)
                    this.level().getRandom().nextLong()
                );

            this.bossEvent.getPlayers().forEach(player -> {
                 player.connection.send(playPacket);
            });

            musicTickTimer = duration;
        }
    }

    private void startJudgement(ServerPlayer player) {
        int killCount = player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        this.judgementTarget = player;
        this.judged = true;
        
        if (killCount == 0) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_0");
        } else if (killCount <= 200) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_200");
        } else if (killCount <= 400) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_400");
        } else if (killCount <= 600) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_600_1", "dialogue.elementmagicboss.skeleton_king.kill_600_2", "dialogue.elementmagicboss.skeleton_king.kill_600_3");
        } else if (killCount <= 800) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_800_1", "dialogue.elementmagicboss.skeleton_king.kill_800_2");
        } else if (killCount <= 1000) {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_1000_1", "dialogue.elementmagicboss.skeleton_king.kill_1000_2");
        } else {
            currentDialogue = List.of("dialogue.elementmagicboss.skeleton_king.kill_max_1", "dialogue.elementmagicboss.skeleton_king.kill_max_2", "dialogue.elementmagicboss.skeleton_king.kill_max_3", "dialogue.elementmagicboss.skeleton_king.kill_max_4");
        }
        
        dialogueIndex = 0;
        dialogueTicks = 0;
        this.playSound(SoundEvents.WITHER_SPAWN, 1.0F, 0.5F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        } else {
            this.bossEvent.setName(Component.translatable("entity.elementmagicboss.skeleton_king.phase", this.getPhase()));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    private void handleDialogue() {
        dialogueTicks++;
        if (dialogueTicks % 60 == 0) { // 3 seconds per line
            if (judgementTarget != null) {
                judgementTarget.sendSystemMessage(Component.translatable("dialogue.elementmagicboss.skeleton_king.prefix").append(Component.translatable(currentDialogue.get(dialogueIndex))));
            }
            dialogueIndex++;
            if (dialogueIndex >= currentDialogue.size()) {
                currentDialogue = null;
                judgementTarget = null;
            }
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    @Override
    public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers) {
        super.registerControllers(controllers);
    }


    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (currentDialogue != null) return false;
        
        if (this.getHealth() - pAmount <= 10.0F && this.getPhase() < 6) {
            this.setHealth(this.getMaxHealth());
            int nextPhase = this.getPhase() + 1;
            this.setPhase(nextPhase);
            
            if (!this.level().isClientSide) {
                // Totem of Undying effect
                this.level().broadcastEntityEvent(this, (byte)35);
                
                final Component mainTitle;
                final Component subTitle;
                
                if (nextPhase < 6) {
                    mainTitle = Component.translatable("title.elementmagicboss.skeleton_king.phase_up");
                    subTitle = Component.translatable("subtitle.elementmagicboss.skeleton_king.determined");
                } else {
                    mainTitle = Component.translatable("title.elementmagicboss.skeleton_king.phase_up");
                    subTitle = Component.translatable("subtitle.elementmagicboss.skeleton_king.special_prepare");
                }

                this.level().players().forEach(p -> {
                    if (p instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(mainTitle));
                        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subTitle));
                        serverPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                    }
                });
            }
            
            this.playSound(SoundEvents.WITHER_DEATH, 1.0F, 1.0F);
            return false;
        }
        
        return super.hurt(pSource, pAmount);
    }

    @Override
    public void die(DamageSource pDamageSource) {
        // LivingHurtEvent でダメージが増幅されてhurt()チェックをすり抜けた場合の安全網
        if (!this.level().isClientSide && this.getPhase() < 6) {
            this.setHealth(this.getMaxHealth());
            int nextPhase = this.getPhase() + 1;
            this.setPhase(nextPhase);
            this.level().broadcastEntityEvent(this, (byte)35);
            final Component mainTitle = Component.translatable("title.elementmagicboss.skeleton_king.phase_up");
            final Component subTitle = nextPhase < 6
                    ? Component.translatable("subtitle.elementmagicboss.skeleton_king.determined")
                    : Component.translatable("subtitle.elementmagicboss.skeleton_king.special_prepare");
            this.level().players().forEach(p -> {
                if (p instanceof ServerPlayer sp) {
                    sp.connection.send(new ClientboundSetTitleTextPacket(mainTitle));
                    sp.connection.send(new ClientboundSetSubtitleTextPacket(subTitle));
                    sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                }
            });
            this.playSound(SoundEvents.WITHER_DEATH, 1.0F, 1.0F);
            return; // 死亡させない
        }
        super.die(pDamageSource);
        if (!this.level().isClientSide) {
            ClientboundStopSoundPacket stopMusic = 
                new ClientboundStopSoundPacket(null, SoundSource.MUSIC);
            ClientboundStopSoundPacket stopHostile = 
                new ClientboundStopSoundPacket(null, SoundSource.HOSTILE);
            
            this.bossEvent.getPlayers().forEach(player -> {
                 player.connection.send(stopMusic);
                 player.connection.send(stopHostile);
            });
            this.serverTriggerEvent(CLIENT_STOP_TRACKING);
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
        this.serverTriggerEvent(CLIENT_START_TRACKING);
        // Initial music trigger if already in combat range
        if (this.getTarget() != null) {
            playPhaseMusic(this.getPhase() <= 3 ? 1 : (this.getPhase() <= 5 ? 2 : 3));
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
        this.serverTriggerEvent(CLIENT_STOP_TRACKING);
        
        ClientboundStopSoundPacket stopMusic = new ClientboundStopSoundPacket(null, SoundSource.MUSIC);
        player.connection.send(stopMusic);
    }

}
