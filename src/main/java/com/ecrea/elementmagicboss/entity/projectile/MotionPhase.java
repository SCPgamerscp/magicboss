package com.ecrea.elementmagicboss.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * YHの CompositeMover / RectMover / ZeroMover に相当する多段階移動システム。
 * 各フェーズで速度と加速度を指定し、弾の tick() で自動的に遷移する。
 *
 * フェーズ内の局所tick t における速度:
 *   velocity(t) = startVelocity + acceleration * t
 *
 * originPos が非null の場合、絶対位置モード:
 *   position(t) = originPos + startVelocity * t + 0.5 * acceleration * t^2
 */
public class MotionPhase {
    public final int duration;
    public final Vec3 startVelocity;
    public final Vec3 acceleration;
    /** null の場合は相対（速度ベース）、非null の場合は絶対位置ベース */
    public final Vec3 originPos;

    public MotionPhase(int duration, Vec3 startVelocity, Vec3 acceleration) {
        this(duration, startVelocity, acceleration, null);
    }

    public MotionPhase(int duration, Vec3 startVelocity, Vec3 acceleration, Vec3 originPos) {
        this.duration = duration;
        this.startVelocity = startVelocity;
        this.acceleration = acceleration;
        this.originPos = originPos;
    }

    /** 静止フェーズ (ZeroMover 相当) */
    public static MotionPhase wait(int duration) {
        return new MotionPhase(duration, Vec3.ZERO, Vec3.ZERO);
    }

    /** 絶対位置で等加速度運動 (RectMover 相当) */
    public static MotionPhase rect(int duration, Vec3 origin, Vec3 velocity, Vec3 accel) {
        return new MotionPhase(duration, velocity, accel, origin);
    }

    /** 定速直進 */
    public static MotionPhase linear(int duration, Vec3 velocity) {
        return new MotionPhase(duration, velocity, Vec3.ZERO);
    }

    /** 加速度つき（相対） */
    public static MotionPhase accel(int duration, Vec3 startVel, Vec3 accel) {
        return new MotionPhase(duration, startVel, accel);
    }

    /**
     * 局所 tick t における速度を返す
     */
    public Vec3 getVelocity(int localTick) {
        return startVelocity.add(acceleration.scale(localTick));
    }

    /**
     * originPos が設定されている場合、局所 tick t における絶対位置を返す
     */
    public Vec3 getAbsolutePosition(int localTick) {
        if (originPos == null) return null;
        return originPos
                .add(startVelocity.scale(localTick))
                .add(acceleration.scale(0.5 * localTick * localTick));
    }

    // ---- NBT 永続化 ----

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Duration", duration);
        tag.putDouble("VX", startVelocity.x);
        tag.putDouble("VY", startVelocity.y);
        tag.putDouble("VZ", startVelocity.z);
        tag.putDouble("AX", acceleration.x);
        tag.putDouble("AY", acceleration.y);
        tag.putDouble("AZ", acceleration.z);
        if (originPos != null) {
            tag.putBoolean("HasOrigin", true);
            tag.putDouble("OX", originPos.x);
            tag.putDouble("OY", originPos.y);
            tag.putDouble("OZ", originPos.z);
        }
        return tag;
    }

    public static MotionPhase fromTag(CompoundTag tag) {
        int duration = tag.getInt("Duration");
        Vec3 vel = new Vec3(tag.getDouble("VX"), tag.getDouble("VY"), tag.getDouble("VZ"));
        Vec3 acc = new Vec3(tag.getDouble("AX"), tag.getDouble("AY"), tag.getDouble("AZ"));
        Vec3 origin = null;
        if (tag.getBoolean("HasOrigin")) {
            origin = new Vec3(tag.getDouble("OX"), tag.getDouble("OY"), tag.getDouble("OZ"));
        }
        return new MotionPhase(duration, vel, acc, origin);
    }

    public static ListTag toListTag(List<MotionPhase> phases) {
        ListTag list = new ListTag();
        for (MotionPhase phase : phases) {
            list.add(phase.toTag());
        }
        return list;
    }

    public static List<MotionPhase> fromListTag(ListTag list) {
        List<MotionPhase> phases = new ArrayList<>();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag ct) {
                phases.add(fromTag(ct));
            }
        }
        return phases;
    }
}
