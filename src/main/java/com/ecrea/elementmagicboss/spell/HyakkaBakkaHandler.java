package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.spells.ExtendedFireworkRocket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 百破爆花ハンドラー
 * ターゲット地点の半径10ブロックに花火を連続スケジュール実行する
 */
@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public final class HyakkaBakkaHandler {

    public static final int FIREWORK_COUNT = 40;
    public static final int BLAST_RADIUS   = 10;
    /** 40発を何tickに渡って発射するか (1発ごとの平均間隔 = SPREAD_TICKS / FIREWORK_COUNT) */
    private static final int SPREAD_TICKS  = 80;

    private static final List<PendingFirework> PENDING = new ArrayList<>();

    private HyakkaBakkaHandler() {}


    public static void schedule(ServerLevel level, LivingEntity caster, Vec3 center,
                                float damage, AbstractSpell spell) {
        long startTick = level.getGameTime();
        Random rng = new Random();
        for (int i = 0; i < FIREWORK_COUNT; i++) {
            // ランダムな遅延 (0 〜 SPREAD_TICKS)
            long delay = (long) (rng.nextDouble() * SPREAD_TICKS);
            // 半径10ブロック以内のランダムな XZ 座標
            double angle    = rng.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(rng.nextDouble()) * BLAST_RADIUS;
            double ox = Math.cos(angle) * distance;
            double oz = Math.sin(angle) * distance;
            Vec3 spawnPos = center.add(ox, 0, oz);
            PENDING.add(new PendingFirework(level.dimension(), caster.getUUID(),
                    spawnPos, damage, spell, startTick + delay));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || PENDING.isEmpty()) return;

        Iterator<PendingFirework> it = PENDING.iterator();
        while (it.hasNext()) {
            PendingFirework fw = it.next();
            ServerLevel level = event.getServer().getLevel(fw.dimension());
            if (level == null) { it.remove(); continue; }
            if (level.getGameTime() < fw.executeTick()) continue;

            Entity casterEntity = level.getEntity(fw.casterId());
            if (casterEntity instanceof LivingEntity caster && caster.isAlive()) {
                spawnFirework(level, caster, fw.spawnPos(), fw.damage(), fw.spell());
            }
            it.remove();
        }
    }


    private static void spawnFirework(ServerLevel level, LivingEntity caster,
                                      Vec3 pos, float damage, AbstractSpell spell) {
        ItemStack rocket = buildRocket(level.random);
        ExtendedFireworkRocket fw = new ExtendedFireworkRocket(
                level, rocket, caster,
                pos.x, pos.y + 0.1, pos.z,
                true, damage);
        level.addFreshEntity(fw);
        // わずかに上向きのランダム射出（花が咲くように広がる）
        double vx = (level.random.nextDouble() - 0.5) * 0.3;
        double vy = 0.2 + level.random.nextDouble() * 0.3;
        double vz = (level.random.nextDouble() - 0.5) * 0.3;
        fw.shoot(vx, vy, vz, 0.5f, 0);
    }

    private static ItemStack buildRocket(net.minecraft.util.RandomSource rng) {
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        CompoundTag props = new CompoundTag();
        ListTag explosions = new ListTag();
        CompoundTag exp = new CompoundTag();

        // 爆発タイプ: 0=小, 2=大, 4=星, ランダム
        byte[] types = {0, 2, 4};
        exp.putByte("Type", types[rng.nextInt(types.length)]);
        if (rng.nextInt(3) == 0) exp.putByte("Trail",   (byte) 1);
        if (rng.nextInt(2) == 0) exp.putByte("Flicker", (byte) 1);
        exp.putIntArray("Colors", pickColors(rng));

        explosions.add(exp);
        props.put("Explosions", explosions);
        props.putByte("Flight", (byte) -1); // 即爆発
        rocket.addTagElement("Fireworks", props);
        return rocket;
    }

    private static int[] pickColors(net.minecraft.util.RandomSource rng) {
        return new int[]{ COLORS[rng.nextInt(COLORS.length)],
                          COLORS[rng.nextInt(COLORS.length)] };
    }

    private static final int[] COLORS = {
        16711680, 16753920, 16776960, 65280,
        255, 8388736, 16711935, 16777215,
        16701501, 13061821, 8439583, 15961002
    };

    private record PendingFirework(
            ResourceKey<Level> dimension,
            UUID casterId,
            Vec3 spawnPos,
            float damage,
            AbstractSpell spell,
            long executeTick
    ) {}
}
