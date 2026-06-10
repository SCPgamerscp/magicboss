package com.ecrea.elementmagicboss.entity.ai;

import com.ecrea.elementmagicboss.entity.ElementEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.EnumSet;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.util.Mth;

public class ElementAttackGoal extends Goal {
    private final ElementEntity mob;
    private LivingEntity target;
    private int spellAttackIntervalMin;
    private int spellAttackIntervalMax;
    private float spellcastingRange;
    private float spellcastingRangeSqr;
    private int seeTime;
    private int spellAttackDelay = -1;
    private int strafeTime = -1;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int fleeCooldown;
    private boolean allowFleeing = true; // Enabled by default
    private double speedModifier;

    public ElementAttackGoal(ElementEntity mob, double speedModifier, int intervalMin, int intervalMax, float range) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.spellAttackIntervalMin = intervalMin;
        this.spellAttackIntervalMax = intervalMax;
        this.spellcastingRange = range;
        this.spellcastingRangeSqr = range * range;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        LivingEntity livingentity = this.mob.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            this.target = livingentity;
            return true;
        } else {
            return false;
        }
    }

    public boolean canContinueToUse() {
        return this.canUse() && this.target.isAlive() && !this.mob.getNavigation().isDone();
    }

    public void stop() {
        this.target = null;
        this.seeTime = 0;
        this.spellAttackDelay = -1;
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    public void tick() {
        if (target == null) {
            return;
        }

        double distanceSquared = this.mob.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
        boolean hasLineOfSight = this.mob.getSensing().hasLineOfSight(this.target);
        if (hasLineOfSight) {
            this.seeTime++;
        } else {
            this.seeTime--;
        }

        doMovement(distanceSquared, hasLineOfSight);

        if (this.mob.isCasting()) {
             var spellData = mob.getMagicData().getCastingSpell();
             if (spellData != null && (target.isDeadOrDying() || spellData.getSpell().shouldAIStopCasting(spellData.getLevel(), mob, target))) {
                this.mob.cancelCast();
             }
             return; // Don't try to attack while casting
        }

        handleAttackLogic(distanceSquared);
    }
    
    protected void doMovement(double distanceSquared, boolean hasLineOfSight) {
        // Stop moving while casting to look more imposing/stable
        if (this.mob.isCasting()) {
            this.mob.getNavigation().stop();
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        
        float fleeDist = 0.15f; // Reduced flee distance (only if VERY close)
        
        if (allowFleeing && --fleeCooldown <= 0 && distanceSquared < spellcastingRangeSqr * (fleeDist * fleeDist)) {
            // Push away/Teleport logic (Fleeing)
            net.minecraft.world.phys.Vec3 flee = net.minecraft.world.entity.ai.util.DefaultRandomPos.getPosAway(this.mob, 16, 7, target.position());
            if (flee != null) {
                this.mob.getNavigation().moveTo(flee.x, flee.y, flee.z, this.speedModifier * 1.3);
            } else {
                 this.mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), -this.speedModifier);
            }
        } else if (distanceSquared < spellcastingRangeSqr && seeTime >= 5) {
            // In range and can see: Stand ground or slowly strafe
            this.mob.getNavigation().stop();
            
            // Only strafe occasionally to avoid "chokomaka" (restless) look
            if (++strafeTime > 40) {
                if (this.mob.getRandom().nextDouble() < 0.1) {
                    strafingClockwise = !strafingClockwise;
                    strafeTime = 0;
                }
            }
            
            // Very slow strafe if needed, otherwise stand still
            // float strafeForward = 0.0f; 
        } else {
            // Chase but update path less frequently to reduce jitter
            if (this.mob.tickCount % 10 == 0) {
                this.mob.getNavigation().moveTo(this.target, this.speedModifier);
            }
        }
    }

    protected void handleAttackLogic(double distanceSquared) {
         if (seeTime < -50) {
            return;
         }
         
         if (this.spellAttackDelay == -1) {
              resetSpellAttackTimer(distanceSquared);
         }
         
         if (--this.spellAttackDelay == 0) {
            doSpellAction();
            resetSpellAttackTimer(distanceSquared);
         }
    }

    protected void resetSpellAttackTimer(double distanceSquared) {
        float f = (float) Math.sqrt(distanceSquared) / this.spellcastingRange;
        this.spellAttackDelay = Math.max(1, (int)(f * (float) (this.spellAttackIntervalMax - this.spellAttackIntervalMin) + (float) this.spellAttackIntervalMin));
    }
    
    protected void doSpellAction() {
        AbstractSpell spell;
        double random = this.mob.getRandom().nextDouble();

        // Expanded spell list for variety
        if (random < 0.2) {
            spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.FIREBALL_SPELL.get();
        } else if (random < 0.4) {
            spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.LIGHTNING_LANCE_SPELL.get();
        } else if (random < 0.6) {
            spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.MAGIC_MISSILE_SPELL.get();
        } else if (random < 0.8) {
            spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.ICICLE_SPELL.get();
        } else {
            spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.BLAZE_STORM_SPELL.get();
        }
        
        // Fixed high level for boss
        int spellLevel = 5; 
        
        if (!spell.shouldAIStopCasting(spellLevel, mob, target)) {
             this.mob.initiateCastSpell(spell, spellLevel);
             this.fleeCooldown = 15 + spell.getCastTime(spellLevel); // Longer cooldown after cast = less frantic movement
        } else {
             this.spellAttackDelay = 5;
        }
    }
}
