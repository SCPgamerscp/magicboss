package com.ecrea.elementmagicboss.entity.ai;

import com.ecrea.elementmagicboss.entity.SkeletonKingEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkeletonKingAttackGoal extends WizardAttackGoal {
    private final SkeletonKingEntity skeletonKing;

    // 内部クラスの代わりに並列リストでコンボキューを管理
    private final List<AbstractSpell> queueSpells = new ArrayList<>();
    private final List<Integer>       queueLevels = new ArrayList<>();

    public SkeletonKingAttackGoal(SkeletonKingEntity mob, double speedModifier, int attackIntervalMin, int attackIntervalMax) {
        super(mob, speedModifier, attackIntervalMin, attackIntervalMax);
        this.skeletonKing = mob;
    }

    @Override
    protected void resetSpellAttackTimer(double distanceSquared) {
        if (!queueSpells.isEmpty()) {
            this.spellAttackDelay = 3;
        } else {
            super.resetSpellAttackTimer(distanceSquared);
        }
    }

    @Override
    protected void doSpellAction() {
        if (this.skeletonKing.isCasting()) return;

        LivingEntity target = this.skeletonKing.getTarget();
        if (target == null) return;

        if (queueSpells.isEmpty()) {
            queueRandomCombo();
            this.spellAttackDelay = 0;
        }

        if (!queueSpells.isEmpty()) {
            AbstractSpell next  = queueSpells.remove(0);
            int           level = queueLevels.remove(0);

            // Oakskin は既にバフ中ならスキップ
            if (next.equals(SpellRegistry.OAKSKIN_SPELL.get()) && this.skeletonKing.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                queueSpells.clear();
                queueLevels.clear();
                this.spellAttackDelay = 20;
                return;
            }

            this.skeletonKing.initiateCastSpell(next, level);
        }
    }

    private void queueRandomCombo() {
        int phase     = this.skeletonKing.getPhase();
        int baseBonus = (phase <= 5) ? (phase - 1) * 5 : 30;
        int level     = 10;

        List<String> pool = new ArrayList<>();
        pool.add("blood_slash");
        pool.add("wither_skull");
        pool.add("acid_orb");
        pool.add("heal");
        pool.add("frost_step");
        pool.add("ray_of_frost");
        pool.add("shockwave");
        pool.add("poison_arrow");
        pool.add("oakskin");
        pool.add("starfall");

        Collections.shuffle(pool);
        String selected = pool.get(0);

        switch (selected) {
            case "blood_slash"   -> queueRepeat(SpellRegistry.BLOOD_SLASH_SPELL.get(),   level, 10 + baseBonus);
            case "wither_skull"  -> queueRepeat(SpellRegistry.WITHER_SKULL_SPELL.get(),  level, 10 + baseBonus);
            case "acid_orb"      -> queueRepeat(SpellRegistry.ACID_ORB_SPELL.get(),      level, 1);
            case "heal"          -> queueRepeat(SpellRegistry.HEAL_SPELL.get(),          level, 5  + baseBonus);
            case "frost_step"    -> queueRepeat(SpellRegistry.FROST_STEP_SPELL.get(),    level, 5  + baseBonus);
            case "ray_of_frost"  -> queueRepeat(SpellRegistry.RAY_OF_FROST_SPELL.get(),  level, 7  + baseBonus);
            case "shockwave"     -> queueRepeat(SpellRegistry.SHOCKWAVE_SPELL.get(),     level, 10 + baseBonus);
            case "poison_arrow"  -> queueRepeat(SpellRegistry.POISON_ARROW_SPELL.get(),  level, 10 + baseBonus);
            case "oakskin"       -> queueRepeat(SpellRegistry.OAKSKIN_SPELL.get(),       level, 1);
            case "starfall"      -> queueRepeat(SpellRegistry.STARFALL_SPELL.get(),      level, 1);
        }
    }

    private void queueRepeat(AbstractSpell spell, int level, int count) {
        for (int i = 0; i < count; i++) {
            queueSpells.add(spell);
            queueLevels.add(level);
        }
    }
}
