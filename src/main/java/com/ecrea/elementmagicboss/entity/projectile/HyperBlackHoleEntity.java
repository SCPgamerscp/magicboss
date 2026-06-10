package com.ecrea.elementmagicboss.entity.projectile;

import com.ecrea.elementmagicboss.entity.ModEntities;
import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.List;

/**
 * 超大質量ブラックホール。
 * 吸引範囲＝半径30ブロック（BlackHole準拠）。
 * ダメージも半径30内全体に1tick50回与える（無敵時間無視）。
 */
public class HyperBlackHoleEntity extends BlackHole {

    private static final float RADIUS = 30f;
    private static final int HITS_PER_TICK = 50;
    private static final int LIFETIME = 20 * 30; // 30秒

    private float hyperDamage = 5.0f;
    private int hyperAge = 0;

    public HyperBlackHoleEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    public HyperBlackHoleEntity(Level level, LivingEntity owner, float damage) {
        this(ModEntities.HYPER_BLACK_HOLE.get(), level);
        this.setOwner(owner);
        this.hyperDamage = damage;
        this.setDamage(damage);   // BlackHoleのダメージも設定（super.tick内で使われる）
        this.setRadius(RADIUS);
        this.setDuration(LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        var bb = this.getBoundingBox();
        float radius = getRadius();
        Vec3 center = bb.getCenter();
        List<Entity> trackingEntities = this.level().getEntities(this, bb.inflate(1.0));

        for (Entity entity : trackingEntities) {
            // 所有者や味方は除外、スペクテイターも除外
            if (entity != getOwner() && !DamageSources.isFriendlyFireBetween(getOwner(), entity) && !entity.isSpectator()) {
                float distance = (float) center.distanceTo(entity.position());
                double dmgRadius = Math.min(2.0, radius / 5.0);
                // ダメージ範囲はBlackHole準拠: dmgRadius = Math.min(2.0, radius/5.0) の2乗以内
                if (distance < dmgRadius * dmgRadius && canHitEntity(entity)) {
                    for (int i = 0; i < HITS_PER_TICK; i++) {
                        entity.invulnerableTime = 0; // 無敵時間無視
                        if (entity instanceof net.minecraftforge.entity.PartEntity<?> part && part.getParent() != null) {
                            part.getParent().invulnerableTime = 0;
                        }
                        DamageSources.applyDamage(entity, this.hyperDamage, io.redspace.ironsspellbooks.api.registry.SpellRegistry.BLACK_HOLE_SPELL.get().getDamageSource(this, getOwner()).setIFrames(0));
                    }
                }
            }
        }
    }
}
