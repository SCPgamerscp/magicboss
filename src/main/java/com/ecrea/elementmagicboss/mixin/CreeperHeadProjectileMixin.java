package com.ecrea.elementmagicboss.mixin;

import com.ecrea.elementmagicboss.accessor.ChainChainAccessor;
import io.redspace.ironsspellbooks.entity.spells.creeper_head.CreeperHeadProjectile;
import io.redspace.ironsspellbooks.spells.evocation.ChainCreeperSpell;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * CreeperHeadProjectile に「着弾時に必ず連鎖」する機能を追加する。
 * chainChainRemaining > 0 のとき、着弾地点に新しいリングを生成する。
 */
@Mixin(CreeperHeadProjectile.class)
public class CreeperHeadProjectileMixin implements ChainChainAccessor {

    /** チェインチェインクリーパー用の残り連鎖回数。0なら通常のChain Creeper動作。 */
    @Unique
    private int elementmagicboss$chainChainRemaining = 0;

    /** 次のリングで召喚する頭部数 */
    @Unique
    private int elementmagicboss$chainChainCount = 0;

    /** 連鎖時のダメージ */
    @Unique
    private float elementmagicboss$chainChainDamage = 0;

    /**
     * onHit の末尾（discardHelper の前）で連鎖リングを生成する。
     * サーバーサイドのみ。
     */
    @Inject(method = "m_6532_", at = @At(value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/entity/spells/creeper_head/CreeperHeadProjectile;discardHelper(Lnet/minecraft/world/phys/HitResult;)V"),
            remap = false)
    private void elementmagicboss$onHitChainChain(HitResult hitResult, CallbackInfo ci) {
        if (elementmagicboss$chainChainRemaining > 0) {
            CreeperHeadProjectile self = (CreeperHeadProjectile) (Object) this;
            Vec3 impactPos = hitResult.getLocation().add(0, 0.5, 0);
            LivingEntity owner = self.getOwner() instanceof LivingEntity lo ? lo : null;

            int nextRemaining = elementmagicboss$chainChainRemaining - 1;
            int count = elementmagicboss$chainChainCount;
            float damage = elementmagicboss$chainChainDamage;

            // ISBのsummonCreeperRingで次のリングを生成
            ChainCreeperSpell.summonCreeperRing(self.level(), owner, impactPos, damage, count);

            // 生成されたヘッドにもchainChain情報を伝搬する
            // summonCreeperRingで生成された直近のエンティティにフラグを設定
            self.level().getEntitiesOfClass(CreeperHeadProjectile.class,
                    new net.minecraft.world.phys.AABB(impactPos, impactPos).inflate(3.0)).forEach(head -> {
                if (head != self && head.tickCount <= 1) {
                    ((CreeperHeadProjectileMixin) (Object) head).elementmagicboss$chainChainRemaining = nextRemaining;
                    ((CreeperHeadProjectileMixin) (Object) head).elementmagicboss$chainChainCount = count;
                    ((CreeperHeadProjectileMixin) (Object) head).elementmagicboss$chainChainDamage = damage;
                }
            });
        }
    }

    /**
     * チェインチェインクリーパー用のデータを設定する。
     * ChainChainCreeperSpell から呼び出される。
     */
    @Unique
    public void elementmagicboss$setChainChainData(int remaining, int count, float damage) {
        this.elementmagicboss$chainChainRemaining = remaining;
        this.elementmagicboss$chainChainCount = count;
        this.elementmagicboss$chainChainDamage = damage;
    }
}
