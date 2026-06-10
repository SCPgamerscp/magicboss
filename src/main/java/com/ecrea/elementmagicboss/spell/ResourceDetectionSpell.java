package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.entity.projectile.ResourceGlowMarkerEntity;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// ResourceDetection: highlights ores and chests within 50 blocks for 30s.
public class ResourceDetectionSpell extends AbstractSpell {

    private static final int SCAN_RADIUS  = 50;

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "resource_detection");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public ResourceDetectionSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of();
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.AMETHYST_CLUSTER_BREAK);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!(world instanceof ServerLevel serverLevel)) {
            super.onCast(world, spellLevel, entity, castSource, playerMagicData);
            return;
        }

        BlockPos center = entity.blockPosition();
        List<BlockPos> found = new ArrayList<>();

        // 全範囲を走査（途中で打ち切らない）
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-SCAN_RADIUS, -SCAN_RADIUS, -SCAN_RADIUS),
                center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            BlockState state = world.getBlockState(pos);
            if (isTarget(state)) {
                found.add(pos.immutable());
            }
        }


        // Spawn glow marker at each found block
        for (BlockPos pos : found) {
            ResourceGlowMarkerEntity marker = new ResourceGlowMarkerEntity(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5);
            serverLevel.addFreshEntity(marker);
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private boolean isTarget(BlockState state) {
        // Ores (vanilla individual ore tags)
        if (state.is(BlockTags.COAL_ORES))     return true;
        if (state.is(BlockTags.IRON_ORES))     return true;
        if (state.is(BlockTags.GOLD_ORES))     return true;
        if (state.is(BlockTags.DIAMOND_ORES))  return true;
        if (state.is(BlockTags.LAPIS_ORES))    return true;
        if (state.is(BlockTags.REDSTONE_ORES)) return true;
        if (state.is(BlockTags.EMERALD_ORES))  return true;
        if (state.is(BlockTags.COPPER_ORES))   return true;
        // Chests and storage blocks
        if (state.is(Blocks.CHEST))          return true;
        if (state.is(Blocks.TRAPPED_CHEST))  return true;
        if (state.is(Blocks.BARREL))         return true;
        if (state.is(Blocks.ENDER_CHEST))    return true;
        if (state.is(Blocks.HOPPER))         return true;
        if (state.is(Blocks.DISPENSER))      return true;
        if (state.is(Blocks.DROPPER))        return true;
        if (state.getBlock() instanceof ShulkerBoxBlock) return true;
        return false;
    }
}