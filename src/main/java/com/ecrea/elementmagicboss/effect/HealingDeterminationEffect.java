package com.ecrea.elementmagicboss.effect;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class HealingDeterminationEffect extends MagicMobEffect {

    public HealingDeterminationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration > 0 && duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        // HP: Lv1=1.0, Lv5=3.0, Lv10=5.5
        entity.heal(1.0f + amplifier * 0.5f);

        // Mana & Food: プレイヤーのみ
        if (entity instanceof ServerPlayer serverPlayer) {
            // Mana: Lv1=10, Lv5=50, Lv10=100
            MagicData magic = MagicData.getPlayerMagicData(serverPlayer);
            magic.addMana(10.0f * (amplifier + 1));
            // クライアントにマナ同期を送信（これがないとマナ表示が更新されない＋自然回復が止まる）
            PacketDistributor.sendToPlayer(serverPlayer, new SyncManaPacket(magic));

            // Food: Lv1=+1 / sat+1.0, Lv5=+5 / sat+3.0, Lv10=+10 / sat+5.5
            FoodData food = serverPlayer.getFoodData();
            int foodGain = 1 + amplifier;
            float satGain = 1.0f + amplifier * 0.5f;
            food.eat(foodGain, satGain);
        }
    }
}