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

/**
 * 剣の舞 - KenNoMaiEffectが有効な間だけ存在する剣エンティティ
 * オーナーからの固定XZオフセットで追従 / 毎tick攻撃 / iFrames=0
 */
public class SpinningSwordEntity extends Entity {

    // 詠唱者からの固定オフセット (ランダム配置)
    private float offsetX = 0f;
    private float offsetZ = 0f;
    private float damage  = 1.0f;
    private UUID  ownerUUID;

    // レンダラー用自転カウンター
    public float prevSpin = 0f;
    public float spin     = 0f;

    public SpinningSwordEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public SpinningSwordEntity(Level level, LivingEntity owner,
                               float damage, float offsetX, float offsetZ) {
        this(ModEntities.SPINNING_SWORD.get(), level);
        this.ownerUUID = owner.getUUID();
        this.damage    = damage;
        this.offsetX   = offsetX;
        this.offsetZ   = offsetZ;
        // 初期位置をオーナー基準オフセットに設定
        this.setPos(owner.getX() + offsetX, owner.getY() + 1.1, owner.getZ() + offsetZ);
    }

    @Override
    public void tick() {
        super.tick();
        prevSpin = spin;
        spin    += 18.0f;

        if (!level().isClientSide) {
            // ownerUUIDなし = /summon で召喚されたデバッグ用エンティティ → その場に留まる
            if (ownerUUID == null) return;

            LivingEntity owner = getOwnerEntity();
            if (owner == null || !owner.isAlive()
                    || !owner.hasEffect(ModMobEffects.KEN_NO_MAI.get())) {
                discard();
                return;
            }
            // 固定オフセットでオーナーに追従
            setPos(owner.getX() + offsetX, owner.getY() + 1.1, owner.getZ() + offsetZ);

            // 毎tick接触ダメージ (MOB向け)
            applyContactDamage(owner);
        }
    }

    /** 剣のBoundingBoxに実際に重なっているLivingEntityに接触ダメージを与える */
    private void applyContactDamage(LivingEntity owner) {
        AABB sword = this.getBoundingBox();
        level().getEntitiesOfClass(LivingEntity.class, sword.inflate(0.1), t ->
                t != owner
                && !owner.isAlliedTo(t)
                && t.isAlive()
                && sword.intersects(t.getBoundingBox())
        ).forEach(target ->
                DamageSources.applyDamage(target, damage,
                        ModSpells.KEN_NO_MAI.get().getDamageSource(this, owner))
        );
    }

    /** プレイヤーが剣に直接触れた時の接触ダメージ */
    @Override
    public void playerTouch(Player player) {
        if (level().isClientSide) return;
        LivingEntity owner = getOwnerEntity(); // /summon時はnull
        if (player == owner || (owner != null && owner.isAlliedTo(player))) return;
        if (!player.isAlive()) return;

        DamageSources.applyDamage(player, damage,
                ModSpells.KEN_NO_MAI.get().getDamageSource(this, owner != null ? owner : this));
    }

    @Nullable
    public LivingEntity getOwnerEntity() {
        if (ownerUUID == null || level().isClientSide) return null;
        var e = ((net.minecraft.server.level.ServerLevel) level()).getEntity(ownerUUID);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        damage  = tag.getFloat("Damage");
        offsetX = tag.getFloat("OffsetX");
        offsetZ = tag.getFloat("OffsetZ");
        if (tag.hasUUID("OwnerUUID")) ownerUUID = tag.getUUID("OwnerUUID");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage",  damage);
        tag.putFloat("OffsetX", offsetX);
        tag.putFloat("OffsetZ", offsetZ);
        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override public boolean isPickable() { return true; }  // 当たり判定ON
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 4096.0; }
}
