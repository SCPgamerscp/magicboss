package com.ecrea.elementmagicboss.mixin;

import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * アルティメットイーターバフ中は満腹でも食事できるようにする。
 * ISSのGluttonyMixinと同じ仕組み。
 */
@Mixin(Player.class)
public class PlayerMixin {

    // Mojang公式名で指定 → Mixinが自動でSRG名にリマップする
    @Inject(method = "canEat", at = @At("RETURN"), cancellable = true)
    private void canEatForUltimateEater(boolean pCanAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        if (((Player)(Object)this).hasEffect(ModMobEffects.ULTIMATE_EATER.get())) {
            cir.setReturnValue(true);
        }
    }
}
