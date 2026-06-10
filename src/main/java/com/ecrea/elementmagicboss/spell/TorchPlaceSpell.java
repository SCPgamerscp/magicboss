package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public class TorchPlaceSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "torch_place");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0.1)
            .build();

    public TorchPlaceSpell() {
        this.manaCostPerLevel   = 0;
        this.baseSpellPower     = 1;
        this.spellPowerPerLevel = 0;
        this.castTime           = 0;
        this.baseManaCost       = 1;
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
        return Optional.of(SoundEvents.WOOD_PLACE);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        BlockHitResult hit = Utils.getTargetBlock(world, entity, ClipContext.Fluid.NONE, 100);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos placePos = hit.getBlockPos().relative(hit.getDirection());
            if (world.getBlockState(placePos).isAir()) {
                // Place wall torch if hitting a horizontal face, else floor torch
                BlockState torchState;
                switch (hit.getDirection()) {
                    case NORTH -> torchState = Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,
                                    net.minecraft.core.Direction.SOUTH);
                    case SOUTH -> torchState = Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,
                                    net.minecraft.core.Direction.NORTH);
                    case EAST -> torchState = Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,
                                    net.minecraft.core.Direction.WEST);
                    case WEST -> torchState = Blocks.WALL_TORCH.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING,
                                    net.minecraft.core.Direction.EAST);
                    default -> torchState = Blocks.TORCH.defaultBlockState();
                }
                world.setBlock(placePos, torchState, 3);
            }
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}