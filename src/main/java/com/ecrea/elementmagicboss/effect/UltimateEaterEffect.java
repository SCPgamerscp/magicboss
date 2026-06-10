package com.ecrea.elementmagicboss.effect;

import com.ecrea.elementmagicboss.spell.ModSpells;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * アルティメットイーターバフ。
 * - 食事時マナ回復量がGluttonyの3倍
 * - 回復力3倍はModEvents.onLivingHealで処理
 */
@Mod.EventBusSubscriber
public class UltimateEaterEffect extends MagicMobEffect {

    public UltimateEaterEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /** 食事完了時にマナを回復 (Gluttonyの3倍) */
    @SubscribeEvent
    public static void onFinishEating(LivingEntityUseItemEvent.Finish event) {
        var entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        var food = event.getItem().getFoodProperties(entity);
        if (food == null) return;
        var effect = entity.getEffect(ModMobEffects.ULTIMATE_EATER.get());
        if (effect == null) return;
        var pmg = MagicData.getPlayerMagicData(entity);
        // Gluttony ratio = (4 + amp) * 0.5、それの3倍
        float ratio = (4 + effect.getAmplifier()) * 0.5f * 3.0f;
        pmg.addMana(food.getNutrition() * ratio);
        if (entity instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new SyncManaPacket(pmg));
        }
    }
}
