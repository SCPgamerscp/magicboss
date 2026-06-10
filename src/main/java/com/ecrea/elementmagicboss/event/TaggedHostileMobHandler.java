package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class TaggedHostileMobHandler {
    private static final String GOALS_ADDED_KEY = "elementmagicboss_tagged_hostile_goals";
    private static final String HOSTILE_MOB_TAG = "hostilemob";
    private static final String HOSTILE_MOB_UNDERSCORE_TAG = "hostile_mob";
    private static final String SHORT_HOSTILE_TAG = "h";
    private static final double ATTACK_SPEED = 1.2D;
    private static final double SHARED_RETALIATION_RANGE = 64.0D;
    private static final float FALLBACK_ATTACK_DAMAGE = 2.0F;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof PathfinderMob mob)) return;
        if (mob.isNoAi()) return;
        if (!isTaggedHostile(mob)) return;
        if (mob.getPersistentData().getBoolean(GOALS_ADDED_KEY)) return;

        if (!keepsNativeRangedOrMagicAttack(mob)) {
            mob.goalSelector.addGoal(2, new TaggedMeleeAttackGoal(mob));
        }
        mob.targetSelector.addGoal(1, new TaggedHurtByTargetGoal(mob));
        mob.targetSelector.addGoal(2, new TaggedPlayerTargetGoal(mob));
        mob.getPersistentData().putBoolean(GOALS_ADDED_KEY, true);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity causingEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        resetInvulnerabilityFromTaggedHostile(event.getEntity(), causingEntity, directEntity);
        shareRetaliationTarget(event.getEntity(), causingEntity, directEntity);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        resetInvulnerabilityFromTaggedHostile(event.getEntity(),
                event.getSource().getEntity(), event.getSource().getDirectEntity());
    }

    private static boolean isTaggedHostile(PathfinderMob mob) {
        return mob.getTags().contains(HOSTILE_MOB_TAG)
                || mob.getTags().contains(HOSTILE_MOB_UNDERSCORE_TAG)
                || mob.getTags().contains(SHORT_HOSTILE_TAG);
    }

    private static boolean keepsNativeRangedOrMagicAttack(PathfinderMob mob) {
        return mob instanceof RangedAttackMob || mob instanceof IMagicEntity;
    }

    private static boolean isValidTarget(Player player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private static boolean isValidTarget(LivingEntity target, PathfinderMob attacker) {
        if (target == null || !target.isAlive() || target == attacker) return false;
        if (target instanceof Player player && !isValidTarget(player)) return false;
        if (target instanceof PathfinderMob mob && isTaggedHostile(mob)) return false;
        return !attacker.isAlliedTo(target) && !target.isAlliedTo(attacker);
    }

    private static void resetInvulnerabilityFromTaggedHostile(
            LivingEntity victim, Entity causingEntity, Entity directEntity) {
        PathfinderMob attacker = getTaggedHostileAttacker(causingEntity, directEntity);
        if (attacker != null && isValidTarget(victim, attacker)) {
            victim.invulnerableTime = 0;
        }
    }

    private static void shareRetaliationTarget(LivingEntity victim, Entity causingEntity, Entity directEntity) {
        if (!(victim instanceof PathfinderMob attackedMob) || !isTaggedHostile(attackedMob)) return;
        LivingEntity attacker = getLivingAttacker(causingEntity, directEntity);
        if (attacker == null || !attacker.isAlive()) return;

        AABB retaliationArea = attackedMob.getBoundingBox().inflate(SHARED_RETALIATION_RANGE);
        attackedMob.level().getEntitiesOfClass(PathfinderMob.class, retaliationArea, mob ->
                mob.isAlive()
                        && !mob.isNoAi()
                        && isTaggedHostile(mob)
                        && isValidTarget(attacker, mob)
        ).forEach(mob -> mob.setTarget(attacker));
    }

    private static LivingEntity getLivingAttacker(Entity causingEntity, Entity directEntity) {
        if (causingEntity instanceof LivingEntity living) return living;
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) {
            return owner;
        }
        return null;
    }

    private static PathfinderMob getTaggedHostileAttacker(Entity causingEntity, Entity directEntity) {
        if (causingEntity instanceof PathfinderMob mob && isTaggedHostile(mob)) return mob;
        if (directEntity instanceof Projectile projectile
                && projectile.getOwner() instanceof PathfinderMob owner
                && isTaggedHostile(owner)) {
            return owner;
        }
        return null;
    }

    private static class TaggedPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final PathfinderMob mob;

        TaggedPlayerTargetGoal(PathfinderMob mob) {
            super(mob, Player.class, 10, true, false,
                    living -> living instanceof Player player && isValidTarget(player));
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return isTaggedHostile(this.mob) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return isTaggedHostile(this.mob) && super.canContinueToUse();
        }
    }

    private static class TaggedHurtByTargetGoal extends HurtByTargetGoal {
        private final PathfinderMob mob;

        TaggedHurtByTargetGoal(PathfinderMob mob) {
            super(mob);
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return isTaggedHostile(this.mob) && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return isTaggedHostile(this.mob) && super.canContinueToUse();
        }
    }

    private static class TaggedMeleeAttackGoal extends Goal {
        private final PathfinderMob mob;
        private int attackCooldown;
        private int pathRefreshCooldown;

        TaggedMeleeAttackGoal(PathfinderMob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.hasValidTaggedTarget();
        }

        @Override
        public boolean canContinueToUse() {
            return this.hasValidTaggedTarget();
        }

        @Override
        public void stop() {
            this.mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.mob.getTarget();
            if (!isValidTarget(target, this.mob)) return;

            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.pathRefreshCooldown <= 0) {
                this.mob.getNavigation().moveTo(target, ATTACK_SPEED);
                this.pathRefreshCooldown = 10;
            }

            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }
            double distance = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            if (distance <= this.getAttackReachSqr(target) && this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                this.mob.swing(this.mob.getUsedItemHand());
                this.hurtIgnoringInvulnerability(target);
            }
        }

        private void hurtIgnoringInvulnerability(LivingEntity target) {
            target.invulnerableTime = 0;
            if (!this.mob.doHurtTarget(target)) {
                target.invulnerableTime = 0;
                target.hurt(this.mob.damageSources().mobAttack(this.mob), FALLBACK_ATTACK_DAMAGE);
            }
            target.invulnerableTime = 0;
        }

        private boolean hasValidTaggedTarget() {
            if (!isTaggedHostile(this.mob) || this.mob.isNoAi()) return false;
            if (keepsNativeRangedOrMagicAttack(this.mob)) return false;
            return isValidTarget(this.mob.getTarget(), this.mob);
        }

        private double getAttackReachSqr(LivingEntity target) {
            float reach = this.mob.getBbWidth() * 2.0F;
            return reach * reach + target.getBbWidth();
        }
    }
}
