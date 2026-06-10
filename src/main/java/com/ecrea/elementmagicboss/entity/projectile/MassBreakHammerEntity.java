package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Invisible logic entity: breaks pre-collected blocks at tick 13 (after SpectralHammer swing).
public class MassBreakHammerEntity extends Entity {

    private static final int BREAK_TICK   = 13;
    private static final int DISCARD_TICK = 32;

    private final List<BlockPos> toBreak = new ArrayList<>();
    private UUID ownerUUID;
    private boolean didBreak = false;

    public MassBreakHammerEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
    }

    public MassBreakHammerEntity(Level level, LivingEntity owner, List<BlockPos> blocksToBreak) {
        this(ModEntities.MASS_BREAK_HAMMER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.toBreak.addAll(blocksToBreak);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        if (tickCount >= BREAK_TICK && !didBreak && level() instanceof ServerLevel serverLevel) {
            didBreak = true;
            LivingEntity owner = getOwnerEntity(serverLevel);
            for (BlockPos pos : toBreak) {
                if (!serverLevel.getBlockState(pos).isAir()) {
                    List<ItemStack> drops = Block.getDrops(
                            serverLevel.getBlockState(pos), serverLevel, pos,
                            serverLevel.getBlockEntity(pos), owner, ItemStack.EMPTY);
                    serverLevel.removeBlock(pos, false);
                    for (ItemStack stack : drops) {
                        ItemEntity ie = new ItemEntity(
                                serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                        serverLevel.addFreshEntity(ie);
                    }
                }
            }
        }

        if (tickCount >= DISCARD_TICK) discard();
    }

    private LivingEntity getOwnerEntity(ServerLevel level) {
        if (ownerUUID == null) return null;
        var e = level.getEntity(ownerUUID);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
        ListTag list = tag.getList("Blocks", Tag.TAG_LONG);
        for (Tag t : list) toBreak.add(BlockPos.of(((LongTag) t).getAsLong()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        ListTag list = new ListTag();
        toBreak.forEach(pos -> list.add(LongTag.valueOf(pos.asLong())));
        tag.put("Blocks", list);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean isPickable() { return false; }
}