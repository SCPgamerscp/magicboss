package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.TheWorldSpell;
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimeStopFieldEntity extends Entity {
    private static final double FREEZE_RADIUS = 512.0D;
    private static final double MIN_ARROW_RADIUS = 4.0D;
    private static final double MAX_ARROW_RADIUS = 6.0D;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
    private static final float RELEASE_SPEED = 1.4f;

    private UUID ownerUUID;
    private UUID targetUUID;
    private float arrowDamage;
    private boolean arrowsSpawned;
    private Vec3 lastKnownTargetCenter = Vec3.ZERO;
    private final Map<UUID, FrozenState> frozenStates = new HashMap<>();
    private final List<UUID> arrowIds = new ArrayList<>();

    public TimeStopFieldEntity(EntityType<? extends TimeStopFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public TimeStopFieldEntity(Level level, LivingEntity owner, LivingEntity target, float arrowDamage) {
        this(ModEntities.TIME_STOP_FIELD.get(), level);
        this.ownerUUID = owner.getUUID();
        this.targetUUID = target.getUUID();
        this.arrowDamage = arrowDamage;
        this.lastKnownTargetCenter = target.getBoundingBox().getCenter();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity owner = getOwnerEntity();
        LivingEntity target = getTargetEntity();
        if (target != null) {
            lastKnownTargetCenter = target.getBoundingBox().getCenter();
            setPos(target.getX(), target.getY(), target.getZ());
        }

        if (level().isClientSide) {
            return;
        }

        if (!arrowsSpawned) {
            spawnFrozenArrows(owner, target);
            arrowsSpawned = true;
        }

        if (tickCount >= TheWorldSpell.DURATION_TICKS) {
            releaseArrows(target);
            unfreezeEntities();
            discard();
            return;
        }

        maintainFrozenArrows(target);
        freezeEntities(owner);
    }

    private void spawnFrozenArrows(@Nullable LivingEntity owner, @Nullable LivingEntity target) {
        Vec3 center = target != null ? target.getBoundingBox().getCenter() : lastKnownTargetCenter;
        for (int i = 0; i < TheWorldSpell.ARROW_COUNT; i++) {
            Vec3 direction = fibonacciDirection(i, TheWorldSpell.ARROW_COUNT);
            double radius = Mth.lerp(level().getRandom().nextDouble(), MIN_ARROW_RADIUS, MAX_ARROW_RADIUS);
            Vec3 spawnPos = center.add(direction.scale(radius));

            SmallMagicArrow arrow = new SmallMagicArrow(level(), owner);
            arrow.setOwner(owner);
            arrow.setDamage(arrowDamage);
            arrow.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            arrow.addTag(TheWorldSpell.THE_WORLD_ARROW_TAG);
            arrow.setNoGravity(true);
            arrow.setDeltaMovement(Vec3.ZERO);
            orientArrow(arrow, center.subtract(spawnPos));
            level().addFreshEntity(arrow);
            arrowIds.add(arrow.getUUID());
        }
    }

    private void maintainFrozenArrows(@Nullable LivingEntity target) {
        Vec3 targetCenter = target != null ? target.getBoundingBox().getCenter() : lastKnownTargetCenter;
        for (UUID arrowId : arrowIds) {
            Entity entity = findEntity(arrowId);
            if (!(entity instanceof SmallMagicArrow arrow) || arrow.isRemoved()) {
                continue;
            }
            arrow.setNoGravity(true);
            arrow.setDeltaMovement(Vec3.ZERO);
            orientArrow(arrow, targetCenter.subtract(arrow.position()));
        }
    }

    private void freezeEntities(@Nullable LivingEntity owner) {
        AABB area = new AABB(position(), position()).inflate(FREEZE_RADIUS);
        List<LivingEntity> livingEntities = level().getEntitiesOfClass(LivingEntity.class, area, entity -> shouldFreeze(entity, owner));
        for (LivingEntity living : livingEntities) {
            freezeEntity(living);
        }

        List<Projectile> projectiles = level().getEntitiesOfClass(Projectile.class, area, entity -> shouldFreeze(entity, owner));
        for (Projectile projectile : projectiles) {
            freezeEntity(projectile);
        }
    }

    public static boolean isFrozenByActiveField(Level level, Entity entity) {
        if (level.isClientSide || entity == null || !entity.isAlive() || entity.isRemoved()) {
            return false;
        }

        AABB area = entity.getBoundingBox().inflate(FREEZE_RADIUS);
        List<TimeStopFieldEntity> fields = level.getEntitiesOfClass(TimeStopFieldEntity.class, area,
                field -> field.isAlive() && field.tickCount < TheWorldSpell.DURATION_TICKS);
        for (TimeStopFieldEntity field : fields) {
            if (field.shouldFreeze(entity, field.getOwnerEntity())) {
                return true;
            }
        }
        return false;
    }

    /** 指定エンティティがアクティブなTimeStopFieldのオーナーかどうかを確認する */
    public static boolean isOwnerOfActiveField(Level level, Entity entity) {
        if (level.isClientSide || entity == null) return false;
        AABB area = entity.getBoundingBox().inflate(FREEZE_RADIUS);
        List<TimeStopFieldEntity> fields = level.getEntitiesOfClass(TimeStopFieldEntity.class, area,
                field -> field.isAlive() && field.tickCount < TheWorldSpell.DURATION_TICKS);
        for (TimeStopFieldEntity field : fields) {
            LivingEntity owner = field.getOwnerEntity();
            if (owner != null && owner.getUUID().equals(entity.getUUID())) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldFreeze(Entity entity, @Nullable LivingEntity owner) {
        if (!entity.isAlive() || entity.isRemoved() || entity == this || entity == owner) {
            return false;
        }
        if (entity instanceof TimeStopFieldEntity) {
            return false;
        }
        if (entity.getTags().contains(TheWorldSpell.THE_WORLD_ARROW_TAG)) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof Projectile;
    }

    private void freezeEntity(Entity entity) {
        FrozenState state = frozenStates.computeIfAbsent(entity.getUUID(), uuid -> FrozenState.capture(entity));
        state.applyFreeze(entity);
    }

    private void unfreezeEntities() {
        for (FrozenState state : frozenStates.values()) {
            Entity entity = findEntity(state.uuid);
            if (entity == null || entity.isRemoved()) {
                continue;
            }
            if (isFrozenByAnotherField(entity)) {
                continue;
            }
            state.restore(entity);
        }
        frozenStates.clear();
    }

    private boolean isFrozenByAnotherField(Entity entity) {
        AABB area = new AABB(position(), position()).inflate(FREEZE_RADIUS);
        List<TimeStopFieldEntity> fields = level().getEntitiesOfClass(TimeStopFieldEntity.class, area,
                field -> field != this && field.isAlive() && field.tickCount < TheWorldSpell.DURATION_TICKS);
        for (TimeStopFieldEntity field : fields) {
            if (field.shouldFreeze(entity, field.getOwnerEntity())) {
                return true;
            }
        }
        return false;
    }

    private void releaseArrows(@Nullable LivingEntity target) {
        Vec3 targetCenter = target != null ? target.getBoundingBox().getCenter() : lastKnownTargetCenter;
        for (UUID arrowId : arrowIds) {
            Entity entity = findEntity(arrowId);
            if (entity instanceof SmallMagicArrow arrow) {
                Vec3 motion = targetCenter.subtract(arrow.position()).normalize().scale(RELEASE_SPEED);
                arrow.setNoGravity(true);
                arrow.shoot(motion);
                orientArrow(arrow, motion);
            }
        }
    }

    @Nullable
    private Entity findEntity(UUID uuid) {
        if (uuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(uuid);
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        Entity entity = findEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Nullable
    public LivingEntity getTargetEntity() {
        Entity entity = findEntity(targetUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static Vec3 fibonacciDirection(int index, int total) {
        double t = (index + 0.5D) / total;
        double y = 1.0D - 2.0D * t;
        double radius = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
        double theta = GOLDEN_ANGLE * index;
        return new Vec3(Math.cos(theta) * radius, y, Math.sin(theta) * radius);
    }

    private static void orientArrow(SmallMagicArrow arrow, Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6D) {
            return;
        }
        double horizontal = direction.horizontalDistance();
        float yRot = (float) (Mth.atan2(direction.x, direction.z) * Mth.RAD_TO_DEG);
        float xRot = (float) (Mth.atan2(direction.y, horizontal) * Mth.RAD_TO_DEG);
        arrow.setYRot(yRot);
        arrow.setXRot(xRot);
        arrow.yRotO = yRot;
        arrow.xRotO = xRot;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.hasUUID("TargetUUID")) {
            targetUUID = tag.getUUID("TargetUUID");
        }
        arrowDamage = tag.getFloat("ArrowDamage");
        arrowsSpawned = tag.getBoolean("ArrowsSpawned");
        if (tag.contains("TargetX")) {
            lastKnownTargetCenter = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
        }
        if (targetUUID != null) {
            tag.putUUID("TargetUUID", targetUUID);
        }
        tag.putFloat("ArrowDamage", arrowDamage);
        tag.putBoolean("ArrowsSpawned", arrowsSpawned);
        tag.putDouble("TargetX", lastKnownTargetCenter.x);
        tag.putDouble("TargetY", lastKnownTargetCenter.y);
        tag.putDouble("TargetZ", lastKnownTargetCenter.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 2048.0D * 2048.0D;
    }

    private static final class FrozenState {
        private final UUID uuid;
        private final Vec3 position;
        private final Vec3 delta;
        private final float yRot;
        private final float xRot;
        private final boolean noAi;
        private final boolean noGravity;

        private FrozenState(UUID uuid, Vec3 position, Vec3 delta, float yRot, float xRot, boolean noAi, boolean noGravity) {
            this.uuid = uuid;
            this.position = position;
            this.delta = delta;
            this.yRot = yRot;
            this.xRot = xRot;
            this.noAi = noAi;
            this.noGravity = noGravity;
        }

        private static FrozenState capture(Entity entity) {
            boolean noAi = entity instanceof Mob mob && mob.isNoAi();
            return new FrozenState(entity.getUUID(), entity.position(), entity.getDeltaMovement(),
                    entity.getYRot(), entity.getXRot(), noAi, entity.isNoGravity());
        }

        private void applyFreeze(Entity entity) {
            entity.setPos(position.x, position.y, position.z);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.setYRot(yRot);
            entity.setXRot(xRot);
            entity.yRotO = yRot;
            entity.xRotO = xRot;
            entity.hurtMarked = true;
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
            }
            if (entity instanceof ServerPlayer player) {
                player.connection.teleport(position.x, position.y, position.z, yRot, xRot);
            }
            if (entity instanceof Projectile projectile) {
                projectile.setNoGravity(true);
            }
        }

        private void restore(Entity entity) {
            entity.setDeltaMovement(delta);
            entity.setNoGravity(noGravity);
            if (entity instanceof Mob mob) {
                mob.setNoAi(noAi);
            }
        }
    }
}
