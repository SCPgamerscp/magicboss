package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.effect.ModMobEffects;
import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;

public class ShoeEntity extends Entity {

    private UUID ownerUUID;
    private float damage = 1.0f;

    // Fixed horizontal offset from owner (set at spawn)
    private double offsetX;
    private double offsetZ;

    // Independent bob phase offset (radians, randomized per shoe at spawn)
    private double phaseOffset;

    public ShoeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * @param offsetX  horizontal X offset from owner
     * @param offsetZ  horizontal Z offset from owner
     * @param phaseOffset  random phase (0 - 2*PI) for independent bob
     */
    public ShoeEntity(Level level, LivingEntity owner, float damage,
                      double offsetX, double offsetZ, double phaseOffset) {
        this(ModEntities.SHOE.get(), level);
        this.ownerUUID   = owner.getUUID();
        this.damage      = damage;
        this.offsetX     = offsetX;
        this.offsetZ     = offsetZ;
        this.phaseOffset = phaseOffset;
        // All shoes start at same height: owner feet + 1.5
        this.setPos(owner.getX() + offsetX, owner.getY() + 1.5, owner.getZ() + offsetZ);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        LivingEntity owner = getOwnerEntity();
        if (owner == null || !owner.isAlive()
                || !owner.hasEffect(ModMobEffects.SHOES_OF_DETERMINATION.get())) {
            discard();
            return;
        }

        // Follow owner horizontally; all shoes at same base height (owner.Y + 1.5)
        // Each shoe bobs independently using its own phaseOffset
        // Amplitude 1.5 -> total range 3 blocks, period ~3s (60 ticks)
        double bob = Math.sin((tickCount / 10.0) + phaseOffset) * 1.5;
        double tx  = owner.getX() + offsetX;
        double ty  = owner.getY() + 1.5 + bob;
        double tz  = owner.getZ() + offsetZ;
        setPos(tx, ty, tz);

        // Contact damage
        AABB hitBox = getBoundingBox().inflate(0.3);
        level().getEntitiesOfClass(LivingEntity.class, hitBox,
            t -> t != owner && !owner.isAlliedTo(t) && t.isAlive()
        ).forEach(target -> {
            target.invulnerableTime = 0;
            DamageSources.applyDamage(target, damage,
                ModSpells.SHOES_OF_DETERMINATION.get().getDamageSource(this, owner));
        });
    }

    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide) return;
        LivingEntity owner = getOwnerEntity();
        if (player == owner || (owner != null && owner.isAlliedTo(player))) return;
        if (!player.isAlive()) return;
        player.invulnerableTime = 0;
        DamageSources.applyDamage(player, damage,
            ModSpells.SHOES_OF_DETERMINATION.get().getDamageSource(
                this, owner != null ? owner : this));
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || level().isClientSide) return null;
        var e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID);
        return e instanceof LivingEntity le ? le : null;
    }

    @Nullable public UUID getOwnerUUID() { return ownerUUID; }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage      = tag.getFloat("Damage");
        offsetX     = tag.getDouble("OffsetX");
        offsetZ     = tag.getDouble("OffsetZ");
        phaseOffset = tag.getDouble("PhaseOffset");
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage);
        tag.putDouble("OffsetX", offsetX);
        tag.putDouble("OffsetZ", offsetZ);
        tag.putDouble("PhaseOffset", phaseOffset);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean isPickable() { return false; }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 4096.0; }
}