package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.MassBreakHammerEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.spectral_hammer.SpectralHammer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MassBreakHammerSpell extends AbstractSpell {

    private static final int LOOK_DISTANCE = 16;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "mass_break_hammer");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(2)
            .build();

    public MassBreakHammerSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower   = 4;
        this.spellPowerPerLevel = 1;
        this.castTime  = 0;
        this.baseManaCost = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", getRadius(spellLevel, caster))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.STONE_BREAK);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity instanceof ServerPlayer sp && sp.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            sp.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.translatable("ui.irons_spellbooks.cast_error_adventure").withStyle(ChatFormatting.RED)));
            return false;
        }
        var hit = Utils.getTargetBlock(level, entity, ClipContext.Fluid.NONE, LOOK_DISTANCE);
        boolean ok = hit.getType() == HitResult.Type.BLOCK && !level.getBlockState(hit.getBlockPos()).isAir();
        if (!ok && entity instanceof ServerPlayer sp) {
            sp.connection.send(new ClientboundSetActionBarTextPacket(
                    Component.literal("No block in sight!").withStyle(ChatFormatting.RED)));
        }
        return ok;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        var blockHit = Utils.getTargetBlock(world, entity, ClipContext.Fluid.NONE, LOOK_DISTANCE);
        if (blockHit.getType() != HitResult.Type.BLOCK) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        BlockPos targetPos   = blockHit.getBlockPos();
        BlockState targetState = world.getBlockState(targetPos);
        if (targetState.isAir()) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        // -- SpectralHammer visual --
        Direction face = blockHit.getDirection();
        SpectralHammer spectralHammer = new SpectralHammer(world, entity, blockHit, 0, 0);
        Vec3 position = Vec3.atCenterOf(targetPos);
        if (!face.getAxis().isVertical()) {
            position = position.subtract(0, 2, 0).subtract(entity.getForward().normalize().scale(1.5));
        } else if (face == Direction.DOWN) {
            position = position.subtract(0, 3, 0);
        }
        spectralHammer.setPos(position.x, position.y, position.z);
        world.addFreshEntity(spectralHammer);

        // -- Collect matching blocks --
        if (world instanceof ServerLevel serverLevel) {
            Block targetBlock = targetState.getBlock();
            int radius = getRadius(spellLevel, entity);

            List<BlockPos> toBreak = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(
                    targetPos.offset(-radius, -radius, -radius),
                    targetPos.offset(radius, radius, radius))) {
                if (world.getBlockState(pos).getBlock() == targetBlock) {
                    toBreak.add(pos.immutable());
                }
            }

            // Spawn invisible logic entity that breaks blocks at tick 13 (after hammer swing)
            MassBreakHammerEntity breaker = new MassBreakHammerEntity(world, entity, toBreak);
            breaker.setPos(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
            serverLevel.addFreshEntity(breaker);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private int getRadius(int spellLevel, LivingEntity caster) {
        return (int) getSpellPower(spellLevel, caster);
    }
}