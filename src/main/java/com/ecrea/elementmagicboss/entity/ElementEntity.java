package com.ecrea.elementmagicboss.entity;

import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.ExtendedServerBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.client.util.BossbarManager;
import com.ecrea.elementmagicboss.item.ModItems;
import com.ecrea.elementmagicboss.sound.ModSounds;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.List;
import java.util.UUID;

public class ElementEntity extends AbstractSpellCastingMob implements IClientEventEntity {

    private static final BossbarManager.BossbarSprite BOSSBAR_SPRITE = new BossbarManager.BossbarSprite(
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_empty.png"),
        new ResourceLocation(ElementMagicBossMod.MOD_ID, "textures/gui/boss_bars/wizard_full.png"),
        256, 100, 0, 20, 256, 100);

    @Override
    public void handleClientEvent(byte eventId) {}

    private ExtendedServerBossEvent bossEvent;
    private int phase = 0;
    private static final int MAX_PHASES = 10;
    private static final float HP_THRESHOLD = 50.0f;
    // フェーズ変化後のクールダウン (同tick内での連続フェーズ消費を防ぐ)
    private static final int PHASE_CHANGE_COOLDOWN = 60; // 3秒
    private int phaseChangeCooldown = 0;
    private int musicTick = 0;
    private static final int MUSIC_LOOP_1_TICKS = 6000;
    private static final int MUSIC_LOOP_2_TICKS = 5100;

    public ElementEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.xpReward = 100000;
        this.bossEvent = (ExtendedServerBossEvent) new ExtendedServerBossEvent(
            this.getUUID(),
            Component.translatable("entity.elementmagicboss.element_entity"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossEvent.setVisible(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSpellCastingMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(AttributeRegistry.MAX_MANA.get(), 100000.0D)
                .add(AttributeRegistry.COOLDOWN_REDUCTION.get(), 100.0D)
                .add(AttributeRegistry.CAST_TIME_REDUCTION.get(), 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new com.ecrea.elementmagicboss.entity.ai.ElementWizardAttackGoal(this, 1.25D, 10, 30)
                .setSpells(
                    List.of(
                        SpellRegistry.FIREBALL_SPELL.get(),
                        SpellRegistry.LIGHTNING_LANCE_SPELL.get(),
                        SpellRegistry.MAGIC_MISSILE_SPELL.get(),
                        SpellRegistry.ICICLE_SPELL.get(),
                        SpellRegistry.BLAZE_STORM_SPELL.get(),
                        SpellRegistry.GUIDING_BOLT_SPELL.get()
                    ),
                    List.of(SpellRegistry.SHIELD_SPELL.get(), SpellRegistry.ARROW_VOLLEY_SPELL.get()),
                    List.of(SpellRegistry.FROST_STEP_SPELL.get()),
                    List.of()
                )
        );
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.getHealth() - pAmount <= HP_THRESHOLD && this.phase < MAX_PHASES - 1) {
            int oldPhase = this.phase;
            this.setHealth(this.getMaxHealth());
            this.phase++;
            this.bossEvent.setName(Component.translatable("entity.elementmagicboss.element_entity.phase", this.phase + 1));
            this.bossEvent.setProgress(1.0f);
            this.playSound(SoundEvents.END_PORTAL_SPAWN, 1.0F, 1.0F);
            if (oldPhase < 5 && this.phase >= 5) {
                this.musicTick = 0;
                triggerMusicToAll(true);
            }
            return false;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected boolean shouldDropLoot() { return true; }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.GREAT_WIZARD_RING.get()));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
        playCurrentMusicTo(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
        stopAllBossMusic(pPlayer);
    }

    @Override
    public void tick() {
        super.tick();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (!this.level().isClientSide) {
            musicTick++;
            int currentMax = (this.phase < 5) ? MUSIC_LOOP_1_TICKS : MUSIC_LOOP_2_TICKS;
            if (musicTick >= currentMax) {
                musicTick = 0;
                triggerMusicToAll(false);
            }
        }
    }

    private void triggerMusicToAll(boolean stopPrevious) {
        List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D));
        for (ServerPlayer player : players) {
            if (stopPrevious) stopAllBossMusic(player);
            playCurrentMusicTo(player);
        }
    }

    private void playCurrentMusicTo(ServerPlayer player) {
        SoundEvent sound = (this.phase < 5) ?
            ModSounds.BATTLE_MUSIC_1.get() :
            ModSounds.BATTLE_MUSIC_2.get();
        // ForgeRegistries でMODサウンドのキー付きHolderを取得
        var holder = ForgeRegistries.SOUND_EVENTS.getHolder(sound.getLocation());
        if (holder.isPresent()) {
            player.connection.send(new ClientboundSoundPacket(
                holder.get(), SoundSource.MUSIC,
                player.getX(), player.getY(), player.getZ(),
                1.0f, 1.0f, player.getRandom().nextLong()));
        }
    }

    private void stopAllBossMusic(ServerPlayer player) {
        player.connection.send(new ClientboundStopSoundPacket(
            ModSounds.BATTLE_MUSIC_1.get().getLocation(), SoundSource.MUSIC));
        player.connection.send(new ClientboundStopSoundPacket(
            ModSounds.BATTLE_MUSIC_2.get().getLocation(), SoundSource.MUSIC));
    }

    public int getPhase() { return this.phase; }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override
    public int getExperienceReward() { return 100000; }

    @Override
    public void die(DamageSource pDamageSource) {
        // LivingHurtEvent でダメージが増幅されてhurt()チェックをすり抜けた場合の安全網
        if (!this.level().isClientSide && this.phase < MAX_PHASES - 1) {
            int oldPhase = this.phase;
            this.phase++;
            this.setHealth(this.getMaxHealth());
            this.bossEvent.setName(Component.translatable("entity.elementmagicboss.element_entity.phase", this.phase + 1));
            this.bossEvent.setProgress(1.0f);
            this.playSound(SoundEvents.END_PORTAL_SPAWN, 1.0F, 1.0F);
            if (oldPhase < 5 && this.phase >= 5) {
                this.musicTick = 0;
                triggerMusicToAll(true);
            }
            return; // 死亡させない
        }
        super.die(pDamageSource);
        if (!this.level().isClientSide) {
            List<ServerPlayer> players = this.level().getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(64.0D));
            for (ServerPlayer player : players) stopAllBossMusic(player);
            int xp = 100000;
            while (xp > 0) {
                int drop = ExperienceOrb.getExperienceValue(xp);
                xp -= drop;
                this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY(), this.getZ(), drop));
            }
        }
    }
}
