package com.ecrea.elementmagicboss.event;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import com.ecrea.elementmagicboss.effect.ModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import com.ecrea.elementmagicboss.enchantment.ModEnchantments;
import com.ecrea.elementmagicboss.enchantment.CustomProtectionEnchantment;
import com.ecrea.elementmagicboss.item.ModItems;
import com.ecrea.elementmagicboss.item.PowerfulBlessingRingItem;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = ElementMagicBossMod.MOD_ID)
public class EnchantmentEventHandler {
    private static final UUID HEALTH_UUID = UUID.fromString("753a3e6c-e2f7-4a0b-93ca-7128e4697b0a");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("6d3e3f6a-e2f7-4a0b-93ca-7128e4697b0b");
    private static final UUID ARMOR_UUID = UUID.fromString("a13e3f6a-e2f7-4a0b-93ca-7128e4697b0c");
    private static final UUID ATTACK_UUID = UUID.fromString("b23e3f6a-e2f7-4a0b-93ca-7128e4697b0d");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        
        // --- Damage Reduction Logic ---
        float multiplier = 1.0f;
        for (ItemStack stack : entity.getArmorSlots()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ULTIMATE_PROTECTION.get(), stack) > 0) multiplier *= 0.5f;
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ADVANCED_PROTECTION.get(), stack) > 0) multiplier *= 0.6f;
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.GREATER_PROTECTION.get(), stack) > 0) multiplier *= 0.7f;
        }
        
        if (multiplier < 1.0f) {
            event.setAmount(event.getAmount() * multiplier);
        }

        int blessingRingCount = CuriosApi.getCuriosHelper()
                .findCurios(entity, stack -> stack.getItem() == ModItems.POWERFUL_BLESSING_RING.get()).size();
        if (blessingRingCount > 0) {
            event.setAmount(event.getAmount() * (float) Math.pow(PowerfulBlessingRingItem.DAMAGE_MULTIPLIER_PER_RING, blessingRingCount));
        }

        // --- Launch (Skyrocket) Logic ---
        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SKYROCKET.get(), weapon) > 0) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.LEVITATION, 5, 99));
            }
            
            // --- Magic Weakness Logic ---
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MAGIC_WEAKNESS.get(), weapon) > 0) {
                var blight = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks", "blight"));
                var slowed = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks", "slowed"));
                
                if (blight != null) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(blight, 600, 19)); // 30s * 20t = 600, lvl 20 = amp 19
                }
                if (slowed != null) {
                    entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(slowed, 600, 19));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {
        if (event.isCanceled()) return;

        int totalLevel = 0;
        for (ItemStack stack : event.getEntity().getArmorSlots()) {
            totalLevel += EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.REGENERATION_DETERMINATION.get(), stack);
        }

        if (totalLevel > 0) {
            event.setAmount(event.getAmount() * (float) Math.pow(2.0D, totalLevel));
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        updateAttributeModifiers(entity);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // --- True Mending Logic (Every 20 ticks = 1 second) ---
        if (entity.tickCount % 20 == 0) {
            for (ItemStack stack : entity.getAllSlots()) {
                if (stack.isDamaged() && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TRUE_MENDING.get(), stack) > 0) {
                    stack.setDamageValue(Math.max(0, stack.getDamageValue() - 5));
                }
            }
        }

        // --- Assassin Logic (Continuous refresh) ---
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ASSASSIN.get(), mainHand) > 0 || 
            EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.ASSASSIN.get(), offHand) > 0) {
            var effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks", "true_invisibility"));
            if (effect != null) {
                entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, 600, 0, false, false, true)); // 30s, no particles, show icon
            }
        }
    }

    private static void updateAttributeModifiers(LivingEntity entity) {
        int healthCount = 0;
        int magicAuraCount = 0;

        for (ItemStack stack : entity.getArmorSlots()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.HEALTH_BOOST.get(), stack) > 0) healthCount++;
            if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.MAGIC_AURA.get(), stack) > 0) magicAuraCount++;
        }

        applyModifier(entity, Attributes.MAX_HEALTH, HEALTH_UUID, "Health Boost", healthCount * 10.0, AttributeModifier.Operation.ADDITION);
        applyModifier(entity, AttributeRegistry.SPELL_POWER.get(), SPELL_POWER_UUID, "Magic Aura Power", magicAuraCount * 0.2, AttributeModifier.Operation.MULTIPLY_BASE);
        applyModifier(entity, Attributes.ARMOR, ARMOR_UUID, "Magic Aura Armor", magicAuraCount * 5.0, AttributeModifier.Operation.ADDITION);
        applyModifier(entity, Attributes.ATTACK_DAMAGE, ATTACK_UUID, "Magic Aura Damage", magicAuraCount * 3.0, AttributeModifier.Operation.ADDITION);
    }

    private static void applyModifier(LivingEntity entity, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid, String name, double value, AttributeModifier.Operation op) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(uuid);
            if (value != 0) {
                instance.addPermanentModifier(new AttributeModifier(uuid, name, value, op));
            }
        }
    }

    // Sure Arrow + Nature Force: apply effects on arrow hit
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        Entity owner = arrow.getOwner();
        if (!(owner instanceof LivingEntity shooter)) return;
        if (!(event.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult entityHit)) return;
        if (!(entityHit.getEntity() instanceof LivingEntity target)) return;

        // Check both hands (works for players and mobs)
        ItemStack main = shooter.getMainHandItem();
        ItemStack off  = shooter.getOffhandItem();

        int sureLevel = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SURE_ARROW.get(), main),
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SURE_ARROW.get(), off));
        int natureLevel = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NATURE_FORCE.get(), main),
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NATURE_FORCE.get(), off));

        if (sureLevel > 0) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.SURE_HIT.get(), sureLevel * 200, 0, false, true, true));
        }
        if (natureLevel > 0) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.BLOOM.get(), natureLevel * 200, 0, false, true, true));
        }
    }

    // Sure Arrow + Nature Force: also trigger on melee attacks
    @SubscribeEvent
    public static void onLivingHurtMelee(LivingHurtEvent event) {
        // Skip arrow hits (handled by onProjectileImpact)
        if (event.getSource().getDirectEntity() instanceof AbstractArrow) return;

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof LivingEntity shooter)) return;

        LivingEntity target = event.getEntity();
        ItemStack main = shooter.getMainHandItem();
        ItemStack off  = shooter.getOffhandItem();

        int sureLevel = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SURE_ARROW.get(), main),
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SURE_ARROW.get(), off));
        int natureLevel = Math.max(
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NATURE_FORCE.get(), main),
                EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.NATURE_FORCE.get(), off));

        if (sureLevel > 0) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.SURE_HIT.get(), sureLevel * 200, 0, false, true, true));
        }
        if (natureLevel > 0) {
            target.addEffect(new MobEffectInstance(
                    ModMobEffects.BLOOM.get(), natureLevel * 200, 0, false, true, true));
        }
    }
}