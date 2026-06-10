package com.ecrea.elementmagicboss.entity.ai;

import com.ecrea.elementmagicboss.entity.ElementEntity;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public class ElementWizardAttackGoal extends WizardAttackGoal {
    private final java.util.List<QueuedSpell> comboQueue = new java.util.ArrayList<>();

    private static class QueuedSpell {
        final io.redspace.ironsspellbooks.api.spells.AbstractSpell spell;
        final int level;
        QueuedSpell(io.redspace.ironsspellbooks.api.spells.AbstractSpell s, int l) { this.spell = s; this.level = l; }
    }

    public ElementWizardAttackGoal(IMagicEntity abstractSpellCastingMob, double pSpeedModifier, int pAttackIntervalMin, int pAttackIntervalMax) {
        super(abstractSpellCastingMob, pSpeedModifier, pAttackIntervalMin, pAttackIntervalMax);
    }

    @Override
    protected void resetSpellAttackTimer(double distanceSquared) {
        if (!comboQueue.isEmpty()) {
            this.spellAttackDelay = 5; 
        } else {
            super.resetSpellAttackTimer(distanceSquared);
        }
    }

    @Override
    protected void doSpellAction() {
        if (this.spellCastingMob.isCasting()) return;

        net.minecraft.world.entity.LivingEntity currentTarget = null;
        if (this.spellCastingMob instanceof net.minecraft.world.entity.Mob) {
            currentTarget = ((net.minecraft.world.entity.Mob)this.spellCastingMob).getTarget();
        }
        if (currentTarget == null) return;

        QueuedSpell next = null;

        if (!comboQueue.isEmpty()) {
            next = comboQueue.get(0);
            // Double-check proximity spells from the queue
            if (next.spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.FLAMING_STRIKE_SPELL.get()) || next.spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.DIVINE_SMITE_SPELL.get())) {
                if (((net.minecraft.world.entity.Entity)this.spellCastingMob).distanceToSqr(currentTarget) > 36.0D) { // 6 blocks buffer (squared)
                    this.comboQueue.clear();
                    next = null;
                } else {
                    this.comboQueue.remove(0);
                }
            } else {
                this.comboQueue.remove(0);
            }
        }

        if (next == null) {
            if (this.spellCastingMob instanceof com.ecrea.elementmagicboss.entity.ElementEntity) {
                com.ecrea.elementmagicboss.entity.ElementEntity elementBoss = (com.ecrea.elementmagicboss.entity.ElementEntity)this.spellCastingMob;
                int phase = elementBoss.getPhase(); 
                
                // Proximity Check: Priority for Flaming Strike and Divine Smite
                double distSq = elementBoss.distanceToSqr(currentTarget);
                if (distSq <= 25.0D) { // 5 blocks
                    if (phase == 3 || phase == 9) { // Ph 4 or Ph 10
                         if (elementBoss.getRandom().nextFloat() < 0.4f) {
                             queueRepeat(io.redspace.ironsspellbooks.api.registry.SpellRegistry.FLAMING_STRIKE_SPELL.get(), 10, 5);
                             next = comboQueue.remove(0);
                         }
                    }
                    if (next == null && (phase == 4 || phase == 9)) { // Ph 5 or Ph 10
                         if (elementBoss.getRandom().nextFloat() < 0.4f) {
                             queueRepeat(io.redspace.ironsspellbooks.api.registry.SpellRegistry.DIVINE_SMITE_SPELL.get(), 10, 5);
                             next = comboQueue.remove(0);
                         }
                    }
                }

                if (next == null) {
                    selectSpellForPhase(phase);
                    if (!comboQueue.isEmpty()) {
                         next = comboQueue.remove(0);
                    }
                }
            }
        }

        if (next != null) {
            if (shouldSkipSpellDueToEffect(next.spell)) {
                this.comboQueue.clear();
                this.spellAttackDelay = 20;
                return;
            }

            this.spellCastingMob.initiateCastSpell(next.spell, next.level);
            this.fleeCooldown = 7 + next.spell.getCastTime(next.level);
        } else {
             super.doSpellAction();
        }
    }

    private void selectSpellForPhase(int phase) {
        int lvl = 10;
        switch (phase) {
            case 0: // Phase 1: Blood
                queueRandom(lvl,
                    "irons_spellbooks:acupuncture:7",
                    "irons_spellbooks:blood_needles:7",
                    "irons_spellbooks:blood_slash:7",
                    "irons_spellbooks:devour:7",
                    "irons_spellbooks:heartstop:1",
                    "irons_spellbooks:ray_of_siphoning:1",
                    "irons_spellbooks:wither_skull:7"
                );
                break;
            case 1: // Phase 2: Ender
                queueRandom(lvl,
                    "irons_spellbooks:dragon_breath:1",
                    "irons_spellbooks:evasion:1",
                    "irons_spellbooks:magic_arrow:7",
                    "irons_spellbooks:magic_missile:10",
                    "irons_spellbooks:guiding_bolt:1" // Leads to Starfall
                );
                break;
            case 2: // Phase 3: Summoning
                queueRandom(lvl,
                    "irons_spellbooks:guiding_bolt:1", // Leads to Arrow Volley x5
                    "irons_spellbooks:chain_creeper:5",
                    "irons_spellbooks:fang_strike:7",
                    "irons_spellbooks:fang_ward:8",
                    "irons_spellbooks:firecracker:10",
                    "irons_spellbooks:gust:7",
                    "irons_spellbooks:lob_creeper:10",
                    "irons_spellbooks:shield:3",
                    "irons_spellbooks:slow:1",
                    "irons_spellbooks:summon_vex:1" // Leads to Raise Dead & Polar Bear
                );
                break;
            case 3: // Phase 4: Fire
                queueRandom(lvl,
                    "irons_spellbooks:guiding_bolt:1", // Leads to Blaze Storm
                    "irons_spellbooks:fire_breath:1",
                    "irons_spellbooks:fireball:5",
                    "irons_spellbooks:firebolt:10",
                    "irons_spellbooks:flaming_barrage:10",
                    "irons_spellbooks:heat_surge:1",
                    "irons_spellbooks:magma_bomb:7",
                    "irons_spellbooks:scorch:7",
                    "irons_spellbooks:wall_of_fire:3"
                );
                break;
            case 4: // Phase 5: Holy
                queueRandom(lvl,
                    "irons_spellbooks:guiding_bolt:10",
                    "irons_spellbooks:heal:1",
                    "irons_spellbooks:sunbeam:7",
                    "irons_spellbooks:wisp:7"
                );
                break;
            case 5: // Phase 6: Ice
                queueRandom(lvl,
                    "irons_spellbooks:cone_of_cold:1",
                    "irons_spellbooks:frost_step:1",
                    "irons_spellbooks:frostwave:1",
                    "irons_spellbooks:ice_block:7",
                    "irons_spellbooks:icicle:10",
                    "irons_spellbooks:ray_of_frost:5"
                );
                break;
            case 6: // Phase 7: Lightning
                queueRandom(lvl,
                    "irons_spellbooks:ascension:1",
                    "irons_spellbooks:guiding_bolt:1", // Leads to Ball Lightning x10
                    "irons_spellbooks:chain_lightning:7",
                    "irons_spellbooks:charge:1",
                    "irons_spellbooks:electrocute:1",
                    "irons_spellbooks:lightning_bolt:7",
                    "irons_spellbooks:lightning_lance:7",
                    "irons_spellbooks:shockwave:7",
                    "irons_spellbooks:thunderstorm:1"
                );
                break;
            case 7: // Phase 8: Nature
                queueRandom(lvl,
                    "irons_spellbooks:acid_orb:1",
                    "irons_spellbooks:blight:1",
                    "irons_spellbooks:earthquake:1",
                    "irons_spellbooks:firefly_swarm:7",
                    "irons_spellbooks:oakskin:1",
                    "irons_spellbooks:poison_arrow:7",
                    "irons_spellbooks:poison_breath:1",
                    "irons_spellbooks:poison_splash:7",
                    "irons_spellbooks:root:1",
                    "irons_spellbooks:spider_aspect:1",
                    "irons_spellbooks:stomp:10"
                );
                break;
            case 8: // Phase 9: Eldritch
                queueRandom(lvl,
                    "irons_spellbooks:eldritch_blast:15",
                    "irons_spellbooks:sculk_tentacles:1",
                    "irons_spellbooks:sonic_boom:5",
                    "irons_spellbooks:black_hole:1"
                );
                break;
            case 9: // Phase 10: Unleashed
                queueRandom(lvl,
                    // Blood
                    "irons_spellbooks:acupuncture:7", "irons_spellbooks:blood_needles:7", "irons_spellbooks:blood_slash:7", "irons_spellbooks:devour:7", "irons_spellbooks:heartstop:1", "irons_spellbooks:ray_of_siphoning:1", "irons_spellbooks:wither_skull:7",
                    // Ender
                    "irons_spellbooks:dragon_breath:1", "irons_spellbooks:magic_arrow:7", "irons_spellbooks:magic_missile:10",
                    // Summon
                    "irons_spellbooks:chain_creeper:5", "irons_spellbooks:fang_strike:7", "irons_spellbooks:fang_ward:8", "irons_spellbooks:firecracker:10", "irons_spellbooks:gust:7", "irons_spellbooks:lob_creeper:10",
                    // Fire
                    "irons_spellbooks:fire_breath:1", "irons_spellbooks:fireball:5", "irons_spellbooks:firebolt:10", "irons_spellbooks:flaming_barrage:10", "irons_spellbooks:heat_surge:1", "irons_spellbooks:magma_bomb:7", "irons_spellbooks:scorch:7", "irons_spellbooks:wall_of_fire:3",
                    // Holy
                    "irons_spellbooks:heal:1", "irons_spellbooks:sunbeam:7", "irons_spellbooks:wisp:7",
                    // Ice
                    "irons_spellbooks:cone_of_cold:1", "irons_spellbooks:frostwave:1", "irons_spellbooks:ice_block:7", "irons_spellbooks:icicle:10", "irons_spellbooks:ray_of_frost:5",
                    // Lightning
                    "irons_spellbooks:chain_lightning:7", "irons_spellbooks:electrocute:1", "irons_spellbooks:lightning_bolt:7", "irons_spellbooks:lightning_lance:7", "irons_spellbooks:shockwave:7",
                    // Nature
                    "irons_spellbooks:acid_orb:1", "irons_spellbooks:blight:1", "irons_spellbooks:earthquake:1", "irons_spellbooks:firefly_swarm:7", "irons_spellbooks:poison_arrow:7", "irons_spellbooks:poison_breath:1", "irons_spellbooks:poison_splash:7", "irons_spellbooks:root:1", "irons_spellbooks:stomp:10",
                    // Eldritch
                    "irons_spellbooks:eldritch_blast:15", "irons_spellbooks:sculk_tentacles:1", "irons_spellbooks:sonic_boom:5", "irons_spellbooks:black_hole:1",
                    // Mix-in Guiding Bolts (Triggering their specific follow-ups)
                    "irons_spellbooks:guiding_bolt:1"
                );
                break;
        }
    }

    private void queueRandom(int level, String... spellSpecs) {
        net.minecraft.world.entity.Mob mob = (net.minecraft.world.entity.Mob) this.spellCastingMob;
        String spec = spellSpecs[mob.getRandom().nextInt(spellSpecs.length)];
        String[] parts = spec.split(":");
        String spellId = parts[0] + ":" + parts[1];
        int count = Integer.parseInt(parts[2]);

        io.redspace.ironsspellbooks.api.spells.AbstractSpell spell = io.redspace.ironsspellbooks.api.registry.SpellRegistry.getSpell(new net.minecraft.resources.ResourceLocation(spellId));
        if (spell != null && !spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.none())) {
            queueRepeat(spell, level, count);

            // Phase-Specific Tactical Combos
            int phase = ((com.ecrea.elementmagicboss.entity.ElementEntity)this.spellCastingMob).getPhase();
            if (spellId.equals("irons_spellbooks:guiding_bolt")) {
                if (phase == 1) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.STARFALL_SPELL.get(), 10, 1);
                else if (phase == 2) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.ARROW_VOLLEY_SPELL.get(), 10, 5);
                else if (phase == 3) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.BLAZE_STORM_SPELL.get(), 10, 1);
                else if (phase == 6) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.BALL_LIGHTNING_SPELL.get(), 10, 10);
                else if (phase == 9) { // Ph 10 chooses random follow-up
                    int r = mob.getRandom().nextInt(4);
                    if (r == 0) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.STARFALL_SPELL.get(), 10, 1);
                    else if (r == 1) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.ARROW_VOLLEY_SPELL.get(), 10, 5);
                    else if (r == 2) addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.BLAZE_STORM_SPELL.get(), 10, 1);
                    else addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.BALL_LIGHTNING_SPELL.get(), 10, 10);
                }
            } else if (spellId.equals("irons_spellbooks:summon_vex") && phase == 2) {
                addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.RAISE_DEAD_SPELL.get(), 10, 1);
                addCombo(io.redspace.ironsspellbooks.api.registry.SpellRegistry.SUMMON_POLAR_BEAR_SPELL.get(), 10, 1);
            }
        }
    }

    private void queueRepeat(io.redspace.ironsspellbooks.api.spells.AbstractSpell spell, int level, int count) {
        for (int i = 0; i < count; i++) {
            comboQueue.add(new QueuedSpell(spell, level));
        }
    }

    private void addCombo(io.redspace.ironsspellbooks.api.spells.AbstractSpell spell, int level, int count) {
        queueRepeat(spell, level, count);
    }

    private boolean shouldSkipSpellDueToEffect(io.redspace.ironsspellbooks.api.spells.AbstractSpell spell) {
        net.minecraft.world.effect.MobEffect effectToCheck = null;

        if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.HEARTSTOP_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.HEARTSTOP.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.EVASION_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.EVASION.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.CHARGE_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.CHARGED.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.THUNDERSTORM_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.THUNDERSTORM.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.OAKSKIN_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.OAKSKIN.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPIDER_ASPECT_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.SPIDER_ASPECT.get();
        else if (spell.equals(io.redspace.ironsspellbooks.api.registry.SpellRegistry.ASCENSION_SPELL.get())) effectToCheck = io.redspace.ironsspellbooks.registries.MobEffectRegistry.ASCENSION.get();

        if (effectToCheck != null) {
            return ((net.minecraft.world.entity.Mob) this.spellCastingMob).hasEffect(effectToCheck);
        }
        return false;
    }
}
