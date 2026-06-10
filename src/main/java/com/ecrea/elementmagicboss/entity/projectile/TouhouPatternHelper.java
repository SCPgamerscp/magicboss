package com.ecrea.elementmagicboss.entity.projectile;

import net.minecraft.world.phys.Vec3;

public final class TouhouPatternHelper {
    private TouhouPatternHelper() {
    }

    public static Basis basis(Vec3 forward) {
        Vec3 f = safeNormalize(forward, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 seedUp = Math.abs(f.y) > 0.95D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
        Vec3 right = safeNormalize(seedUp.cross(f), new Vec3(1.0D, 0.0D, 0.0D));
        Vec3 up = safeNormalize(f.cross(right), new Vec3(0.0D, 1.0D, 0.0D));
        return new Basis(f, right, up);
    }

    public static Vec3 rotate(Basis basis, double yawDegrees, double pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double cosPitch = Math.cos(pitch);
        return safeNormalize(
                basis.forward.scale(cosPitch * Math.cos(yaw))
                        .add(basis.right.scale(cosPitch * Math.sin(yaw)))
                        .add(basis.up.scale(Math.sin(pitch))),
                basis.forward
        );
    }

    public static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double angleRadians) {
        Vec3 normalizedAxis = safeNormalize(axis, new Vec3(0.0D, 1.0D, 0.0D));
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);
        return vector.scale(cos)
                .add(normalizedAxis.cross(vector).scale(sin))
                .add(normalizedAxis.scale(normalizedAxis.dot(vector) * (1.0D - cos)));
    }

    public static Vec3 ringOffset(Basis basis, double angleDegrees, double radius) {
        double angle = Math.toRadians(angleDegrees);
        return basis.right.scale(Math.cos(angle) * radius)
                .add(basis.up.scale(Math.sin(angle) * radius));
    }

    public static Vec3 safeNormalize(Vec3 vec, Vec3 fallback) {
        return vec.lengthSqr() < 1.0E-6D ? fallback : vec.normalize();
    }

    public record Basis(Vec3 forward, Vec3 right, Vec3 up) {
    }
}
