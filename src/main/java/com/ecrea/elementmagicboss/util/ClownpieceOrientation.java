package com.ecrea.elementmagicboss.util;

import net.minecraft.world.phys.Vec3;

/**
 * YH の DanmakuHelper.Orientation を依存なしで再実装。
 */
public class ClownpieceOrientation {

    public final Vec3 forward, normal, side;

    public ClownpieceOrientation(Vec3 forward, Vec3 normal, Vec3 side) {
        this.forward = forward;
        this.normal  = normal;
        this.side    = side;
    }

    public Vec3 rotateDegrees(double deg) {
        double r = deg / 180.0 * Math.PI;
        return side.scale(Math.sin(r)).add(forward.scale(Math.cos(r)));
    }

    public Vec3 rotateDegrees(double deg, double ver) {
        double h = deg / 180.0 * Math.PI;
        double v = ver / 180.0 * Math.PI;
        return side.scale(Math.sin(h) * Math.cos(v))
                .add(forward.scale(Math.cos(h) * Math.cos(v)))
                .add(normal.scale(Math.sin(v)));
    }

    /** asNormal: normal を forward として扱う */
    public ClownpieceOrientation asNormal() {
        return new ClownpieceOrientation(normal, forward, side);
    }

    public static ClownpieceOrientation from(Vec3 dir) {
        double val = dir.x * dir.x + dir.z * dir.z;
        Vec3 ax0 = val < 1e-4
                ? new Vec3(1, 0, 0)
                : new Vec3(-dir.x * dir.y, val, -dir.z * dir.y).normalize();
        Vec3 ax1 = dir.cross(ax0).normalize();
        return new ClownpieceOrientation(dir, ax0, ax1);
    }

    public static ClownpieceOrientation from(Vec3 dir, Vec3 ax0) {
        Vec3 ax1 = dir.cross(ax0).normalize();
        return new ClownpieceOrientation(dir, ax0, ax1);
    }
}
