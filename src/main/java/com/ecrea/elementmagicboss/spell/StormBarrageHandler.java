package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public final class StormBarrageHandler {
    private static final List<PendingStrike> PENDING_STRIKES = new ArrayList<>();

    private StormBarrageHandler() {
    }

    public static void schedule(ServerLevel level, LivingEntity caster, Vec3 center, float damage, AbstractSpell spell) {
        long startTick = level.getGameTime();
        for (int i = 0; i < StormBarrageSpell.STRIKE_COUNT; i++) {
            long executeTick = startTick + (i % StormBarrageSpell.BURST_DURATION_TICKS);
            PENDING_STRIKES.add(new PendingStrike(level.dimension(), caster.getUUID(), center, damage, spell, executeTick));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_STRIKES.isEmpty()) {
            return;
        }

        Iterator<PendingStrike> iterator = PENDING_STRIKES.iterator();
        while (iterator.hasNext()) {
            PendingStrike strike = iterator.next();
            ServerLevel level = event.getServer().getLevel(strike.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }
            if (level.getGameTime() < strike.executeTick()) {
                continue;
            }

            Entity casterEntity = level.getEntity(strike.casterId());
            if (casterEntity instanceof LivingEntity caster && caster.isAlive()) {
                executeStrike(level, caster, strike.center(), strike.damage(), strike.spell());
            }
            iterator.remove();
        }
    }

    private static void executeStrike(ServerLevel level, LivingEntity caster, Vec3 center, float damage, AbstractSpell spell) {
        Vec3 strikePos = findStrikePosition(level, center);
        LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(level);
        if (lightningBolt == null) {
            return;
        }

        lightningBolt.setVisualOnly(true);
        lightningBolt.setDamage(0);
        lightningBolt.setPos(strikePos);
        level.addFreshEntity(lightningBolt);

        float radius = StormBarrageSpell.DAMAGE_RADIUS;
        double radiusSq = radius * radius;
        AABB area = AABB.ofSize(strikePos, radius * 2, radius * 2, radius * 2);
        level.getEntities(caster, area, target -> canHit(caster, target)).forEach(target -> {
            double distance = target.distanceToSqr(strikePos);
            if (distance < radiusSq && Utils.hasLineOfSight(level, strikePos.add(0, 2, 0), target.getBoundingBox().getCenter(), true)) {
                float scaledDamage = (float) (damage * (1 - distance / radiusSq));
                DamageSources.applyDamage(target, scaledDamage, spell.getDamageSource(lightningBolt, caster));
                if (target instanceof Creeper creeper) {
                    creeper.thunderHit(level, lightningBolt);
                }
            }
        });
    }

    private static Vec3 findStrikePosition(ServerLevel level, Vec3 center) {
        double angle = level.random.nextDouble() * (Math.PI * 2);
        double distance = Math.sqrt(level.random.nextDouble()) * StormBarrageSpell.STRIKE_RADIUS;
        Vec3 candidate = center.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
        return Utils.moveToRelativeGroundLevel(level, candidate, 10);
    }

    private static boolean canHit(Entity owner, Entity target) {
        return target != owner && target.isAlive() && target.isPickable() && !target.isSpectator();
    }

    private record PendingStrike(
            ResourceKey<Level> dimension,
            UUID casterId,
            Vec3 center,
            float damage,
            AbstractSpell spell,
            long executeTick
    ) {
    }
}
