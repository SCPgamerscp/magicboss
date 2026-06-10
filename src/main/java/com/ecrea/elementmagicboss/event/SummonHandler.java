package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.entity.ElementEntity;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SummonHandler {

    @SubscribeEvent
    public static void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack itemStack = event.getItemStack();

        if (state.getBlock() == Blocks.RESPAWN_ANCHOR && itemStack.getItem() == ModItems.STAR_OF_THE_GREAT_WIZARD.get()) {
            // Check if Respawn Anchor is charged (optional, but requested logic "put into respawn anchor" usually implies interacting)
            // But standard interaction is right click.
            
            // Summon Boss
            if (level instanceof ServerLevel serverLevel) {
                 ElementEntity boss = ModEntities.ELEMENT_ENTITY.get().create(serverLevel);
                 if (boss != null) {
                     boss.moveTo(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 0, 0);
                     serverLevel.addFreshEntity(boss);
                     
                     // Strike Lightning (Visual only)
                     net.minecraft.world.entity.LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
                     if (lightning != null) {
                        lightning.moveTo(pos.above().getCenter());
                        lightning.setVisualOnly(true);
                        serverLevel.addFreshEntity(lightning);
                     }
                     
                     // Send message
                     serverLevel.players().forEach(player -> player.sendSystemMessage(Component.translatable("message.elementmagicboss.legend_returned")));
                     
                     // Consume Item
                     if (!event.getEntity().isCreative()) {
                         itemStack.shrink(1);
                     }
                     
                     // Prevent GUI open
                     event.setCanceled(true);
                 }
            }
        }
    }
}
