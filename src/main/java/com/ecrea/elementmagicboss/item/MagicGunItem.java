package com.ecrea.elementmagicboss.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

public class MagicGunItem extends Item {
    public MagicGunItem(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public int getEnchantmentValue() {
        return 20; // 弓と同じくらいのエンチャントのしやすさ
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        // 弓のエンチャントを含む本なら許可
        java.util.Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(book);
        for (Enchantment enchant : enchants.keySet()) {
            if (enchant.category == EnchantmentCategory.BOW) {
                return true;
            }
        }
        return super.isBookEnchantable(stack, book);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000; // 1 hour of continuous firing
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if (!pLevel.isClientSide && pLivingEntity instanceof Player) {
            Player pPlayer = (Player) pLivingEntity;
            ItemStack offhandStack = pPlayer.getOffhandItem();
            if (!offhandStack.isEmpty()) {
                fireProjectile(pLevel, pPlayer, offhandStack);
            }
        }
    }

    private void fireProjectile(Level world, Player player, ItemStack stack) {
        Item item = stack.getItem();
        boolean fired = false;

        if (item instanceof ArrowItem) {
            AbstractArrow arrow = ((ArrowItem)item).createArrow(world, stack, player);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
            
            // クリティカル設定 (威力を最大にする)
            arrow.setCritArrow(true);
            
            // エンチャントの適用 (マジックガン自体のエンチャントを参照)
            ItemStack gunStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (gunStack.getItem() != ModItems.MAGIC_GUN.get()) {
                gunStack = player.getItemInHand(InteractionHand.OFF_HAND);
            }
            
            if (gunStack.getItem() == ModItems.MAGIC_GUN.get()) {
                // 射撃 (Power)
                int powerLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, gunStack);
                if (powerLevel > 0) {
                    arrow.setBaseDamage(arrow.getBaseDamage() + (double)powerLevel * 0.5D + 0.5D);
                }
                
                // パンチ (Punch)
                int punchLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, gunStack);
                if (punchLevel > 0) {
                    arrow.setKnockback(punchLevel);
                }
                
                // フレイム (Flame)
                if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, gunStack) > 0) {
                    arrow.setSecondsOnFire(100);
                }
            }

            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            world.addFreshEntity(arrow);
            fired = true;
        } else if (item == Items.TRIDENT) {
            ThrownTrident trident = new ThrownTrident(world, player, stack);
            trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
                trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            world.addFreshEntity(trident);
            fired = true;
        } else if (item == Items.ENDER_EYE) {
            EyeOfEnder eye = new EyeOfEnder(world, player.getX(), player.getEyeY(), player.getZ());
            eye.setItem(stack);
            if (world instanceof ServerLevel) {
                BlockPos blockpos = ((ServerLevel)world).findNearestMapStructure(net.minecraft.tags.StructureTags.EYE_OF_ENDER_LOCATED, player.blockPosition(), 100, false);
                if (blockpos != null) {
                    eye.signalTo(blockpos);
                }
            }
            world.addFreshEntity(eye);
            fired = true;
        } else if (item == Items.TNT) {
            PrimedTnt tnt = new PrimedTnt(world, player.getX(), player.getEyeY(), player.getZ(), player);
            Vec3 vec3 = player.getLookAngle();
            tnt.setDeltaMovement(vec3.scale(1.5D));
            tnt.setFuse(80);
            world.addFreshEntity(tnt);
            fired = true;
        } else if (item == Items.FIREWORK_ROCKET) {
            FireworkRocketEntity firework = new FireworkRocketEntity(world, stack, player, player.getX(), player.getEyeY() - 0.15D, player.getZ(), true);
            firework.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(firework);
            fired = true;
        } else if (item == Items.FIRE_CHARGE) {
            SmallFireball fireball = new SmallFireball(world, player, player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z);
            fireball.setPos(player.getX(), player.getEyeY(), player.getZ());
            world.addFreshEntity(fireball);
            fired = true;
        } else if (item == Items.SNOWBALL) {
            Snowball snowball = new Snowball(world, player);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(snowball);
            fired = true;
        } else if (item == Items.EGG) {
            ThrownEgg egg = new ThrownEgg(world, player);
            egg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(egg);
            fired = true;
        } else if (item == Items.ENDER_PEARL) {
            ThrownEnderpearl pearl = new ThrownEnderpearl(world, player);
            pearl.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            world.addFreshEntity(pearl);
            fired = true;
        } else if (item == Items.SPLASH_POTION || item == Items.LINGERING_POTION) {
            ThrownPotion potion = new ThrownPotion(world, player);
            potion.setItem(stack);
            potion.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            world.addFreshEntity(potion);
            fired = true;
        } else if (item == Items.EXPERIENCE_BOTTLE) {
            ThrownExperienceBottle xpBottle = new ThrownExperienceBottle(world, player);
            xpBottle.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.7F, 1.0F);
            world.addFreshEntity(xpBottle);
            fired = true;
        } else if (item == Items.DRAGON_BREATH) {
            // エンドラの火球
            Vec3 vec3 = player.getLookAngle();
            DragonFireball dragonfireball = new DragonFireball(world, player, vec3.x, vec3.y, vec3.z);
            dragonfireball.setPos(player.getX(), player.getEyeY(), player.getZ());
            world.addFreshEntity(dragonfireball);
            fired = true;
        } else if (item == Items.TOTEM_OF_UNDYING) {
            // エヴォーカの牙 (30列)
            Vec3 look = player.getLookAngle().normalize();
            for (int i = 1; i <= 30; i++) {
                double x = player.getX() + look.x * i;
                double z = player.getZ() + look.z * i;
                double y = player.getY();
                // 地面の高さを探す
                BlockPos pos = new BlockPos((int)x, (int)y, (int)z);
                while (world.getBlockState(pos).isAir() && pos.getY() > world.getMinBuildHeight()) {
                    pos = pos.below();
                }
                while (!world.getBlockState(pos).isAir() && pos.getY() < world.getMaxBuildHeight()) {
                    pos = pos.above();
                }
                world.addFreshEntity(new EvokerFangs(world, x, pos.getY(), z, player.getYRot(), 2, player));
            }
            fired = true;
        } else if (item == Items.SCULK_CATALYST) {
            // ウォーデンの衝撃波 (Sonic Boom)
            if (world instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) world;
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                for (int i = 1; i < 20; i++) {
                    Vec3 point = start.add(look.scale(i));
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0, 0, 0, 0);
                    
                    // 当たり判定
                    AABB box = new AABB(point.x - 1, point.y - 1, point.z - 1, point.x + 1, point.y + 1, point.z + 1);
                    for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, box, EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
                        if (target != player) {
                            target.hurt(world.damageSources().magic(), 10.0F); // 10ダメージ
                            target.setDeltaMovement(target.getDeltaMovement().add(look.scale(0.5))); // ノックバック
                        }
                    }
                }
                world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 3.0F, 1.0F);
            }
            fired = true;
        }

        if (fired) {
            world.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 2.0f);
        }
    }
}
