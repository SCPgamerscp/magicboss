package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public final class SureHitProjectileHandler {

    private static final double SEARCH_RADIUS = 48.0D;
    private static final double MIN_PROJECTILE_SPEED = 0.25D;
    private static final double GUIDED_STRENGTH = 1.85D;

    private SureHitProjectileHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (target.isRemoved() || target.isDeadOrDying() || target.getEffect(ModMobEffects.SURE_HIT.get()) == null) {
            return;
        }

        // Keep the target outlined while Sure Hit is active without needing a custom mixin.
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10, 0, false, false, false));

        AABB area = target.getBoundingBox().inflate(SEARCH_RADIUS);
        List<Projectile> nearbyProjectiles = target.level().getEntitiesOfClass(
                Projectile.class,
                area,
                projectile -> projectile != null && !projectile.isRemoved()
        );

        Vec3 targetCenter = target.getBoundingBox().getCenter();
        for (Projectile projectile : nearbyProjectiles) {
            guideProjectile(projectile, targetCenter);
        }
    }

    private static void guideProjectile(Projectile projectile, Vec3 targetCenter) {
        Vec3 toTarget = targetCenter.subtract(projectile.position());
        if (toTarget.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 motion = projectile.getDeltaMovement();
        double speed = Math.max(motion.length(), MIN_PROJECTILE_SPEED);
        Vec3 homeVector = toTarget.normalize().scale(speed * GUIDED_STRENGTH);
        Vec3 combinedMotion = motion.add(homeVector);
        Vec3 newMotion = combinedMotion.lengthSqr() < 1.0E-6D
                ? toTarget.normalize().scale(speed)
                : combinedMotion.normalize().scale(speed);

        projectile.setDeltaMovement(newMotion);
        projectile.hasImpulse = true;
        projectile.hurtMarked = true;
    }
}
