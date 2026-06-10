package com.ecrea.elementmagicboss.entity.ai;

import com.ecrea.elementmagicboss.entity.AngelRazielEntity;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;

/**
 * Angel Raziel専用の攻撃AI
 * ユーザー指定の特定シーケンスをランダムに実行する
 */
public class AngelRazielAttackGoal extends WizardAttackGoal {
    private enum SequenceType {
        DIVINE_SMITE, GUIDING_BOLT, SUNBEAM, HEAL, LIGHTNING_LANCE, FLAMING_BARRAGE, ROOT_MAGMA, THUNDERSTORM, BALL_LIGHTNING,
        BLAZE_STORM
    }

    private SequenceType currentSequence = null;
    private int sequenceCount = 0;
    private boolean isRootDone = false;

    private final IMagicEntity mobRef;

    public AngelRazielAttackGoal(IMagicEntity entity, double speedModifier, int attackIntervalMin, int attackIntervalMax) {
        super(entity, speedModifier, attackIntervalMin, attackIntervalMax);
        this.mobRef = entity;
        // MOVEフラグを除去することで、AngelRazielFlyGoal（Priority 1）と同時に実行可能にする
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.TARGET));
    }

    @Override
    protected void doMovement(double distanceSquared) {
        if (this.mobRef instanceof AngelRazielEntity) {
            AngelRazielEntity raziel = (AngelRazielEntity) this.mobRef;
            if (raziel.getPhase() == 1) {
                // 第1フェーズ（飛行）では AngelRazielFlyGoal に移動を任せる
                return;
            }
        }
        super.doMovement(distanceSquared);
    }

    @Override
    protected void doSpellAction() {
        if (this.mobRef.isCasting()) return;

        LivingEntity target = ((Mob)this.mobRef).getTarget();
        if (target == null) return;

        double distSq = ((Entity)this.mobRef).distanceToSqr(target);

        // 至近距離（4ブロック以内）なら最優先でDivine Smite 10連射シーケンスを開始
        if (distSq <= 16.0D && currentSequence != SequenceType.DIVINE_SMITE) {
            currentSequence = SequenceType.DIVINE_SMITE;
            sequenceCount = 0;
            isRootDone = false;
        }

        if (currentSequence == null) {
            pickRandomSequence();
        }

        if (currentSequence != null) {
            executeSequence(target);
        }
    }

    private void pickRandomSequence() {
        if (this.mobRef instanceof Mob) {
            Mob mob = (Mob) this.mobRef;
            List<SequenceType> pool = new ArrayList<>();
            
            // 通常魔法を追加
            pool.add(SequenceType.GUIDING_BOLT);
            pool.add(SequenceType.SUNBEAM);
            pool.add(SequenceType.LIGHTNING_LANCE);
            pool.add(SequenceType.FLAMING_BARRAGE);
            pool.add(SequenceType.ROOT_MAGMA);
            pool.add(SequenceType.BALL_LIGHTNING);
            pool.add(SequenceType.BLAZE_STORM);
            
            // 体力が70%以下なら回復も選択肢に入れる (フェーズ制限撤廃)
            if (mob.getHealth() < mob.getMaxHealth() * 0.7f) {
                pool.add(SequenceType.HEAL);
                // 体力がさらに低い場合は優先度を上げる
                if (mob.getHealth() < mob.getMaxHealth() * 0.3f) {
                    pool.add(SequenceType.HEAL);
                }
            }

            // Thunderstorm は効果が出ていない時のみ追加
            if (!mob.hasEffect(io.redspace.ironsspellbooks.registries.MobEffectRegistry.THUNDERSTORM.get())) {
                pool.add(SequenceType.THUNDERSTORM);
                pool.add(SequenceType.THUNDERSTORM); // 優先度高め
            }

            if (!pool.isEmpty()) {
                currentSequence = pool.get(mob.getRandom().nextInt(pool.size()));
            } else {
                currentSequence = SequenceType.GUIDING_BOLT;
            }
            sequenceCount = 0;
            isRootDone = false;
        }
    }

    private void executeSequence(LivingEntity target) {
        AbstractSpell spell = null;
        int maxCount = 0;

        switch (currentSequence) {
            case DIVINE_SMITE:
                spell = SpellRegistry.DIVINE_SMITE_SPELL.get();
                maxCount = 10;
                break;
            case GUIDING_BOLT:
                spell = SpellRegistry.GUIDING_BOLT_SPELL.get();
                maxCount = 15;
                break;
            case SUNBEAM:
                spell = SpellRegistry.SUNBEAM_SPELL.get();
                maxCount = 12;
                break;
            case HEAL:
                spell = SpellRegistry.HEAL_SPELL.get();
                maxCount = 3;
                break;
            case LIGHTNING_LANCE:
                spell = SpellRegistry.LIGHTNING_LANCE_SPELL.get();
                maxCount = 7;
                break;
            case FLAMING_BARRAGE:
                spell = SpellRegistry.FLAMING_BARRAGE_SPELL.get();
                maxCount = 15;
                break;
            case ROOT_MAGMA:
                if (!isRootDone) {
                    spell = SpellRegistry.ROOT_SPELL.get();
                    maxCount = 1;
                } else {
                    spell = SpellRegistry.MAGMA_BOMB_SPELL.get();
                    maxCount = 15;
                }
                break;
            case THUNDERSTORM:
                spell = SpellRegistry.THUNDERSTORM_SPELL.get();
                maxCount = 1;
                // 既にバフがかかっている場合はシーケンスをキャンセルして次へ
                if (this.mobRef instanceof Mob) {
                    if (((Mob)this.mobRef).hasEffect(io.redspace.ironsspellbooks.registries.MobEffectRegistry.THUNDERSTORM.get())) {
                        currentSequence = null;
                        this.spellAttackDelay = 1;
                        return;
                    }
                }
                break;
            case BALL_LIGHTNING:
                spell = SpellRegistry.BALL_LIGHTNING_SPELL.get();
                maxCount = 15;
                break;
            case BLAZE_STORM:
                spell = SpellRegistry.BLAZE_STORM_SPELL.get();
                maxCount = 1; // 1回のみ
                break;
        }

        if (spell != null) {
            int level = 10;
            // Blaze Storm はレベル10で詠唱
            this.mobRef.initiateCastSpell(spell, level);
            sequenceCount++;
            
            // 連射感を出すためにクールダウンを短縮
            if (this.mobRef instanceof AbstractSpellCastingMob) {
                int baseDelay = 2;
                super.spellAttackDelay = baseDelay + spell.getEffectiveCastTime(level, (LivingEntity)this.mobRef);
            }

            if (sequenceCount >= maxCount) {
                // 特殊移行: Rootの後はMagma Bombへ
                if (currentSequence == SequenceType.ROOT_MAGMA && !isRootDone) {
                    isRootDone = true;
                    sequenceCount = 0;
                } else {
                    // シーケンス完了
                    currentSequence = null;
                    if (this.mobRef instanceof Mob) {
                        super.spellAttackDelay = 30 + ((Mob)this.mobRef).getRandom().nextInt(30); // 休憩
                    } else {
                        super.spellAttackDelay = 40;
                    }
                }
            }
        }
    }
}
