package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * VoidStrike: 攻撃者がバフを持っている場合、ダメージを奈落ダメージ(アーマー無視)に変換する。
 *
 * fellOutOfWorld の DamageType を使いつつ、causingEntity に攻撃者を設定することで
 * プレイヤーキルとして認識され、経験値やレアドロップが出るようになる。
 */
@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class VoidStrikeHandler {

    /** 再帰防止フラグ */
    private static boolean processing = false;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (processing) return;

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity livingAttacker)) return;
        if (!livingAttacker.hasEffect(ModMobEffects.VOID_STRIKE.get())) return;

        // 既に奈落ダメージの場合は処理しない
        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) return;

        float damage = event.getAmount();
        LivingEntity target = event.getEntity();

        // 元のダメージイベントをキャンセル
        event.setCanceled(true);

        // アーマー無視の奈落ダメージソースを攻撃者情報付きで作成
        // DamageSource(damageType, causingEntity, directEntity)
        // causingEntity = 攻撃者 → プレイヤーキル判定・経験値に使われる
        DamageSource voidDamage = new DamageSource(
                target.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(DamageTypes.FELL_OUT_OF_WORLD),
                livingAttacker,  // directEntity（直接の加害者）
                livingAttacker   // causingEntity（根本原因）
        );

        processing = true;
        try {
            target.invulnerableTime = 0;
            target.hurt(voidDamage, damage);
        } finally {
            processing = false;
        }
    }
}