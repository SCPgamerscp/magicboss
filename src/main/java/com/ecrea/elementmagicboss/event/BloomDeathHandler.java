package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.OakTreeGrower;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class BloomDeathHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!entity.hasEffect(ModMobEffects.BLOOM.get())) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        BlockPos pos = entity.blockPosition();

        // Place oak sapling then grow it instantly
        BlockPos groundPos = findGround(serverLevel, pos);
        if (groundPos == null) return;

        serverLevel.setBlock(groundPos, Blocks.OAK_SAPLING.defaultBlockState(), 3);
        OakTreeGrower grower = new OakTreeGrower();
        grower.growTree(serverLevel,
                serverLevel.getChunkSource().getGenerator(),
                groundPos,
                Blocks.OAK_SAPLING.defaultBlockState(),
                serverLevel.getRandom());
    }

    private static BlockPos findGround(ServerLevel level, BlockPos start) {
        // Search downward up to 5 blocks for a solid surface
        for (int dy = 0; dy >= -5; dy--) {
            BlockPos check = start.offset(0, dy, 0);
            BlockPos below = check.below();
            if (level.getBlockState(check).isAir()
                    && level.getBlockState(below).isSolidRender(level, below)) {
                return check;
            }
        }
        return start;
    }
}