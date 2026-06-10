package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.sunbeam.SunbeamEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public final class SunStormHandler {

    private static final List<PendingBeam> PENDING_BEAMS = new ArrayList<>();

    private SunStormHandler() {}

    /**
     * キャスター周囲に複数のサンビームをスケジュール登録する。
     */
    public static void schedule(ServerLevel level, LivingEntity caster,
                                Vec3 center, float damage, AbstractSpell spell) {
        long startTick = level.getGameTime();
        for (int i = 0; i < SunStormSpell.BEAM_COUNT; i++) {
            long executeTick = startTick + (long) (i * SunStormSpell.BURST_DURATION_TICKS / SunStormSpell.BEAM_COUNT);
            PENDING_BEAMS.add(new PendingBeam(
                    level.dimension(), caster.getUUID(),
                    center, damage, spell, executeTick));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING_BEAMS.isEmpty()) {
            return;
        }

        Iterator<PendingBeam> iterator = PENDING_BEAMS.iterator();
        while (iterator.hasNext()) {
            PendingBeam beam = iterator.next();
            ServerLevel level = event.getServer().getLevel(beam.dimension());
            if (level == null) {
                iterator.remove();
                continue;
            }
            if (level.getGameTime() < beam.executeTick()) {
                continue;
            }

            Entity casterEntity = level.getEntity(beam.casterId());
            if (casterEntity instanceof LivingEntity caster && caster.isAlive()) {
                spawnBeam(level, caster, beam.center(), beam.damage(), beam.spell());
            }
            iterator.remove();
        }
    }

    private static void spawnBeam(ServerLevel level, LivingEntity caster,
                                  Vec3 center, float damage, AbstractSpell spell) {
        Vec3 spawnPos = findBeamPosition(level, center);

        SunbeamEntity sunbeam = new SunbeamEntity(level);
        sunbeam.setOwner(caster);
        sunbeam.moveTo(spawnPos);
        sunbeam.setDamage(damage);
        level.addFreshEntity(sunbeam);

        level.playSound(null, sunbeam.blockPosition(),
                SoundRegistry.SUNBEAM_WINDUP.get(), SoundSource.NEUTRAL, 2.0f, 1.0f);
    }

    private static Vec3 findBeamPosition(ServerLevel level, Vec3 center) {
        double angle    = level.random.nextDouble() * (Math.PI * 2);
        double distance = Math.sqrt(level.random.nextDouble()) * SunStormSpell.STRIKE_RADIUS;
        Vec3 candidate  = center.add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
        Vec3 ground = Utils.moveToRelativeGroundLevel(level, candidate, 10);
        return ground.add(0, 2, 0);
    }

    private record PendingBeam(
            ResourceKey<Level> dimension,
            UUID casterId,
            Vec3 center,
            float damage,
            AbstractSpell spell,
            long executeTick
    ) {}
}
