package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.DanmakuShotSpell;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;

import java.util.HashSet;
import java.util.Set;

public class DanmakuShotProjectile extends Projectile implements IEntityAdditionalSpawnData {
    public static final int MAX_BOUNCES = 15;
    public static final int MAX_LIFETIME_TICKS = 500;
    private static final int MAX_HITS_PER_TICK = 5;
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(DanmakuShotProjectile.class, EntityDataSerializers.INT);

    public Vec3 deltaMovementOld = Vec3.ZERO;

    private final Set<Integer> entitiesHitThisTick = new HashSet<>();
    private int hitsThisTick;
    private int bounces;
    private int realAge;
    private float damage;
    private String damageSpellId = ModSpells.DANMAKU_SHOT.getId().toString();
    private BlockPos lastHitBlock;

    public DanmakuShotProjectile(EntityType<? extends DanmakuShotProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public DanmakuShotProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.DANMAKU_SHOT_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    /** サブクラスでバウンド回数を変更可能 */
    public int getMaxBounces() {
        return MAX_BOUNCES;
    }

    /** サブクラスで寿命を変更可能 */
    public int getMaxLifetimeTicks() {
        return MAX_LIFETIME_TICKS;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_VARIANT, 0);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void shoot(Vec3 direction) {
        this.setDeltaMovement(direction.normalize().scale(getSpeed()));
    }

    public float getSpeed() {
        return 0.9F;
    }

    @Override
    public void tick() {
        super.baseTick();
        entitiesHitThisTick.clear();
        hitsThisTick = 0;

        if (tickCount == 1) {
            deltaMovementOld = getDeltaMovement();
        }

        // getMaxLifetimeTicks() でサブクラスの寿命を使う
        if (++realAge > getMaxLifetimeTicks()) {
            if (!level().isClientSide) {
                impactParticles(getX(), getBoundingBox().getCenter().y, getZ());
            }
            discard();
            return;
        }

        if (level().isClientSide) {
            trailParticles();
        }

        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hitResult)) {
            this.onHit(hitResult);
        }

        setPos(position().add(getDeltaMovement()));
        rotateWithMotion();
        deltaMovementOld = getDeltaMovement();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        Entity owner = getOwner();
        return super.canHitEntity(target) && target != owner && (owner == null || !owner.isAlliedTo(target));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        if (!entitiesHitThisTick.add(target.getId())) {
            return;
        }

        if (target instanceof LivingEntity livingEntity) {
            DamageSources.ignoreNextKnockback(livingEntity);
        }

        DamageSources.applyDamage(target, damage, resolveDamageSpell().getDamageSource(this, getOwner()));
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();

        if (hitsThisTick++ < MAX_HITS_PER_TICK) {
            HitResult nextHit = ProjectileUtil.getHitResultOnMoveVector(this,
                    entity -> this.canHitEntity(entity) && !entitiesHitThisTick.contains(entity.getId()));
            if (nextHit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, nextHit)) {
                onHit(nextHit);
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        impactParticles(result.getLocation().x, result.getLocation().y, result.getLocation().z);
        playImpactSound();

        BlockPos blockPos = result.getBlockPos();
        if (blockPos.equals(lastHitBlock)) {
            return;
        }
        lastHitBlock = blockPos;

        switch (result.getDirection()) {
            case UP, DOWN -> this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, -1.0D, 1.0D));
            case EAST, WEST -> this.setDeltaMovement(this.getDeltaMovement().multiply(-1.0D, 1.0D, 1.0D));
            case NORTH, SOUTH -> this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 1.0D, -1.0D));
            default -> {
            }
        }

        Vec3 nudgedPos = result.getLocation().add(this.getDeltaMovement().normalize().scale(0.1D));
        setPos(nudgedPos);

        if (++bounces >= getMaxBounces()) {
            discard();
        }
    }

    protected AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(damageSpellId);
        return spell == SpellRegistry.none() ? ModSpells.DANMAKU_SHOT.get() : spell;
    }

    private void rotateWithMotion() {
        Vec3 motion = getDeltaMovement();
        double horizontal = motion.horizontalDistance();
        this.setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(motion.y, horizontal) * Mth.RAD_TO_DEG));
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        } else {
            this.xRotO = rotateContinuously(this.xRotO, this.getXRot());
            this.yRotO = rotateContinuously(this.yRotO, this.getYRot());
        }
    }

    private static float rotateContinuously(float previous, float current) {
        while (current - previous < -180.0F) {
            previous -= 360.0F;
        }
        while (current - previous >= 180.0F) {
            previous += 360.0F;
        }
        return previous;
    }

    protected void trailParticles() {
        Vec3 pos = this.getBoundingBox().getCenter();
        level().addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0.0D, 0.0D, 0.0D);
    }

    protected void impactParticles(double x, double y, double z) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.08D, 0.08D, 0.08D, 0.02D);
        }
    }

    protected void playImpactSound() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", damage);
        tag.putInt("Bounces", bounces);
        tag.putInt("RealAge", realAge);
        tag.putInt("Variant", getVariant());
        tag.putString("DamageSpellId", damageSpellId);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage = tag.getFloat("Damage");
        bounces = tag.getInt("Bounces");
        realAge = tag.getInt("RealAge");
        setVariant(tag.getInt("Variant"));
        if (tag.contains("DamageSpellId")) {
            damageSpellId = tag.getString("DamageSpellId");
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Entity owner = getOwner();
        buffer.writeInt(owner == null ? 0 : owner.getId());
        buffer.writeFloat(damage);
        buffer.writeInt(getVariant());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        Entity owner = level().getEntity(buffer.readInt());
        if (owner != null) {
            setOwner(owner);
        }
        damage = buffer.readFloat();
        setVariant(buffer.readInt());
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public String getDamageSpellId() {
        return damageSpellId;
    }

    public void setDamageSpellId(String damageSpellId) {
        this.damageSpellId = damageSpellId;
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Math.floorMod(variant, DanmakuShotSpell.PELLET_COUNT));
    }
}
