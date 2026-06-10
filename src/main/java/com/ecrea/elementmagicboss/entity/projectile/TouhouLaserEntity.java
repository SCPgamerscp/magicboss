package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class TouhouLaserEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final String IFRAME_TAG = "elementmagicboss_touhou_laser_iframe";

    public enum LaserColor {
        RED(0xFF4A56),
        BLUE(0x4E7BFF),
        GREEN(0x45FF79),
        CYAN(0x4FFFFF),
        MAGENTA(0xFF4CFF),
        YELLOW(0xFFE760);

        private final int rgb;

        LaserColor(int rgb) {
            this.rgb = rgb;
        }

        public int rgb() {
            return rgb;
        }

        public static LaserColor byId(int id) {
            LaserColor[] values = values();
            return values[Math.floorMod(id, values.length)];
        }
    }

    private Vec3 laserDir = new Vec3(0.0D, 0.0D, 1.0D);
    private float damage = 5.0f;
    private float rollSeed;
    private float length = 40.0f;
    private int life = 20;
    private int tickInterval = 1;
    private int colorId;
    private String damageSpellId = ModSpells.DANMAKU_SHOT.getId().toString();
    private UUID ownerUUID;
    @Nullable private LivingEntity cachedOwner;

    public TouhouLaserEntity(EntityType<? extends TouhouLaserEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public TouhouLaserEntity(Level level, @Nullable LivingEntity owner, Vec3 pos, Vec3 dir,
                             float damage, float length, int life, int tickInterval, LaserColor color, String damageSpellId) {
        this(ModEntities.TOUHOU_LASER.get(), level);
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
        this.laserDir = TouhouPatternHelper.safeNormalize(dir, new Vec3(0.0D, 0.0D, 1.0D));
        this.damage = damage;
        this.length = length;
        this.life = life;
        this.tickInterval = Math.max(1, tickInterval);
        this.colorId = color.ordinal();
        this.damageSpellId = damageSpellId;
        this.setPos(pos.x, pos.y, pos.z);
        syncRotationFromDir();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= life) {
            discard();
            return;
        }
        if (!level().isClientSide && tickCount % tickInterval == 0) {
            dealLineDamage();
        }
    }

    private void dealLineDamage() {
        Vec3 start = position();
        Vec3 end = start.add(laserDir.scale(length));
        AABB box = new AABB(start, end).inflate(0.9D);
        LivingEntity owner = getOwner();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != owner && (owner == null || !owner.isAlliedTo(entity)))) {
            double dist = distanceToLine(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), start, laserDir, length);
            if (dist < 1.0D) {
                target.invulnerableTime = 0;
                DamageSources.applyDamage(target, damage, resolveDamageSpell().getDamageSource(this, owner));
                target.invulnerableTime = 0;
            }
        }
    }

    private static double distanceToLine(Vec3 point, Vec3 lineOrigin, Vec3 lineDir, double length) {
        Vec3 diff = point.subtract(lineOrigin);
        double t = diff.dot(lineDir);
        if (t < 0.0D || t > length) {
            return Double.MAX_VALUE;
        }
        return diff.subtract(lineDir.scale(t)).length();
    }

    public float getOpacity(float partialTick) {
        float t = tickCount + partialTick;
        if (t < 4.0F) {
            return 0.2F + t / 4.0F * 0.8F;
        }
        if (t < life - 6.0F) {
            return 1.0F;
        }
        return Math.max(0.0F, (life - t) / 6.0F);
    }

    public LaserColor getLaserColor() {
        return LaserColor.byId(colorId);
    }

    public float getRenderLength() {
        return length;
    }

    public float getRollSeed() {
        return rollSeed;
    }

    public void setRollSeed(float rollSeed) {
        this.rollSeed = rollSeed;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (cachedOwner != null && cachedOwner.isAlive()) {
            return cachedOwner;
        }
        if (ownerUUID != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity living) {
                cachedOwner = living;
                return living;
            }
        }
        return null;
    }

    private AbstractSpell resolveDamageSpell() {
        AbstractSpell spell = SpellRegistry.getSpell(damageSpellId);
        return spell == SpellRegistry.none() ? ModSpells.DANMAKU_SHOT.get() : spell;
    }

    private void syncRotationFromDir() {
        double horizontal = laserDir.horizontalDistance();
        float yRot = -(float) (Mth.atan2(laserDir.x, laserDir.z) * Mth.RAD_TO_DEG);
        float xRot = -(float) (Mth.atan2(laserDir.y, horizontal) * Mth.RAD_TO_DEG);
        this.setYRot(yRot);
        this.yRotO = yRot;
        this.setXRot(xRot);
        this.xRotO = xRot;
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeDouble(laserDir.x);
        buffer.writeDouble(laserDir.y);
        buffer.writeDouble(laserDir.z);
        buffer.writeFloat(damage);
        buffer.writeFloat(rollSeed);
        buffer.writeFloat(length);
        buffer.writeInt(life);
        buffer.writeInt(tickInterval);
        buffer.writeInt(colorId);
        buffer.writeUtf(damageSpellId);
        Entity owner = getOwner();
        buffer.writeInt(owner != null ? owner.getId() : -1);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        laserDir = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        damage = buffer.readFloat();
        rollSeed = buffer.readFloat();
        length = buffer.readFloat();
        life = buffer.readInt();
        tickInterval = buffer.readInt();
        colorId = buffer.readInt();
        damageSpellId = buffer.readUtf();
        int id = buffer.readInt();
        if (id >= 0) {
            Entity entity = level().getEntity(id);
            if (entity instanceof LivingEntity living) {
                cachedOwner = living;
            }
        }
        syncRotationFromDir();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("DirX", laserDir.x);
        tag.putDouble("DirY", laserDir.y);
        tag.putDouble("DirZ", laserDir.z);
        tag.putFloat("Damage", damage);
        tag.putFloat("RollSeed", rollSeed);
        tag.putFloat("Length", length);
        tag.putInt("Life", life);
        tag.putInt("TickInterval", tickInterval);
        tag.putInt("ColorId", colorId);
        tag.putString("DamageSpellId", damageSpellId);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        laserDir = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        damage = tag.getFloat("Damage");
        rollSeed = tag.getFloat("RollSeed");
        length = tag.getFloat("Length");
        life = tag.getInt("Life");
        tickInterval = tag.getInt("TickInterval");
        colorId = tag.getInt("ColorId");
        damageSpellId = tag.getString("DamageSpellId");
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        syncRotationFromDir();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }
}


