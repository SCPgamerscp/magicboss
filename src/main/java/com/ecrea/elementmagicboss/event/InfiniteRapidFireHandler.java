package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.enchantment.ModEnchantments;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class InfiniteRapidFireHandler {

    private static final String IRF_TAG = "InfiniteRapidFire";

    // ================================================================
    // チャージ処理: 弓・クロスボウを即時装填
    // ================================================================
    @SubscribeEvent
    public static void onUseItemTick(LivingEntityUseItemEvent.Tick event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = event.getItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), stack) <= 0) return;

        if (stack.getItem() instanceof BowItem) {
            // 弓: クライアント・サーバー両方でdurationを変更
            int useDuration = stack.getItem().getUseDuration(stack);
            if (event.getDuration() > useDuration - 20) {
                event.setDuration(useDuration - 20);
            }

        } else if (stack.getItem() instanceof CrossbowItem) {
            // クロスボウ: 未装填なら duration=1 にセットしてSTOPで拾う
            if (!CrossbowItem.isCharged(stack)) {
                event.setDuration(1);
            }
        }
    }

    // ================================================================
    // クロスボウ装填完了: STOPイベントをキャンセルして矢・花火を消費せず装填
    // ================================================================
    @SubscribeEvent
    public static void onUseItemStop(LivingEntityUseItemEvent.Stop event) {
        ItemStack stack = event.getItem();
        if (!(stack.getItem() instanceof CrossbowItem)) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), stack) <= 0) return;
        if (CrossbowItem.isCharged(stack)) return;

        LivingEntity entity = event.getEntity();

        // プレイヤーが持っている実際の弾薬を取得（消費はしない）
        ItemStack ammo;
        if (entity instanceof Player player) {
            ammo = player.getProjectile(stack);
        } else {
            ammo = new ItemStack(Items.ARROW);
        }

        // 弾がない場合はクリエイティブ用ダミー
        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

        // バニラの releaseUsing（矢消費）をキャンセル
        event.setCanceled(true);

        // マルチショット対応
        int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, stack);
        int shots = multishot > 0 ? 3 : 1;

        CompoundTag tag = stack.getOrCreateTag();
        ListTag projectileList = new ListTag();
        for (int i = 0; i < shots; i++) {
            // 実際の弾薬の種類（効果つきの矢・花火など）をそのままNBTに保存
            ItemStack copy = ammo.copy();
            copy.setCount(1);
            CompoundTag ammoTag = new CompoundTag();
            copy.save(ammoTag);
            projectileList.add(ammoTag);
        }
        tag.put("ChargedProjectiles", projectileList);
        tag.putBoolean("Charged", true);

        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.CROSSBOW_LOADING_END, SoundSource.PLAYERS,
                1.0F, 1.0F / (entity.level().getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
    }

    // ================================================================
    // クロスボウが撃った矢・花火を拾えないようにする
    // ================================================================
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        // 矢の場合
        if (event.getEntity() instanceof AbstractArrow arrow) {
            if (!(arrow.getOwner() instanceof LivingEntity shooter)) return;
            ItemStack mainhand = shooter.getMainHandItem();
            ItemStack offhand = shooter.getOffhandItem();
            boolean hasCrossbow =
                (mainhand.getItem() instanceof CrossbowItem &&
                 EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), mainhand) > 0) ||
                (offhand.getItem() instanceof CrossbowItem &&
                 EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), offhand) > 0);
            if (hasCrossbow) {
                arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
                arrow.getPersistentData().putBoolean(IRF_TAG, true);
            }
        }

        // 花火の場合
        if (event.getEntity() instanceof FireworkRocketEntity firework) {
            if (!(firework.getOwner() instanceof LivingEntity shooter)) return;
            ItemStack mainhand = shooter.getMainHandItem();
            ItemStack offhand = shooter.getOffhandItem();
            boolean hasCrossbow =
                (mainhand.getItem() instanceof CrossbowItem &&
                 EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), mainhand) > 0) ||
                (offhand.getItem() instanceof CrossbowItem &&
                 EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), offhand) > 0);
            if (hasCrossbow) {
                firework.getPersistentData().putBoolean(IRF_TAG, true);
            }
        }
    }

    // ================================================================
    // 弓専用: 矢を消費しない
    // ================================================================
    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        ItemStack bow = event.getBow();
        if (!(bow.getItem() instanceof BowItem)) return;
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.INFINITE_RAPID_FIRE.get(), bow) <= 0) return;

        event.setCanceled(true);
        Level level = player.level();
        if (level.isClientSide) return;

        ItemStack arrowStack = player.getProjectile(bow);
        if (arrowStack.isEmpty()) {
            if (player.getAbilities().instabuild) {
                arrowStack = new ItemStack(Items.ARROW);
            } else {
                return;
            }
        }

        AbstractArrow arrow;
        if (arrowStack.getItem() instanceof ArrowItem arrowItem) {
            arrow = arrowItem.createArrow(level, arrowStack, player);
        } else {
            arrow = new Arrow(level, player);
        }
        arrow = ((BowItem) bow.getItem()).customArrow(arrow);
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
        arrow.setCritArrow(true);

        int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (powerLevel > 0) arrow.setBaseDamage(arrow.getBaseDamage() + powerLevel * 0.5 + 0.5);
        int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow);
        if (punch > 0) arrow.setKnockback(punch);
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) {
            arrow.setSecondsOnFire(100);
        }

        arrow.getPersistentData().putBoolean(IRF_TAG, true);
        arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        level.addFreshEntity(arrow);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F);

        bow.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    // ================================================================
    // 無敵時間を無視
    // ================================================================
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        net.minecraft.world.entity.Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity == null) return;

        boolean isIRF = false;
        if (directEntity instanceof AbstractArrow arrow) {
            isIRF = arrow.getPersistentData().getBoolean(IRF_TAG);
        } else if (directEntity instanceof FireworkRocketEntity firework) {
            isIRF = firework.getPersistentData().getBoolean(IRF_TAG);
        }

        if (isIRF) {
            LivingEntity victim = event.getEntity();
            victim.invulnerableTime = 0;
            victim.hurtTime = 0;
        }
    }
}
