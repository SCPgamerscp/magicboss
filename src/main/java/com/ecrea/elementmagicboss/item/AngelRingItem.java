package com.ecrea.elementmagicboss.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class AngelRingItem extends Item implements ICurioItem {
    private static final UUID ATTACK_UUID = UUID.fromString("d07b301c-6d98-4c07-b089-af0c8976d801");
    private static final UUID ARMOR_UUID = UUID.fromString("d07b301c-6d98-4c07-b089-af0c8976d802");
    private static final UUID MANA_UUID = UUID.fromString("d07b301c-6d98-4c07-b089-af0c8976d803");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("d07b301c-6d98-4c07-b089-af0c8976d804");

    public AngelRingItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        
        // Attack Damage +6
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(uuid, "Angel Ring Attack", 6.0, AttributeModifier.Operation.ADDITION));
        // Armor +5
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Angel Ring Armor", 5.0, AttributeModifier.Operation.ADDITION));
        // Max Mana +300
        builder.put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(uuid, "Angel Ring Mana", 300.0, AttributeModifier.Operation.ADDITION));
        // Spell Power +20%
        builder.put(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(uuid, "Angel Ring Spell Power", 0.2, AttributeModifier.Operation.MULTIPLY_BASE));

        return builder.build();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide) return;

        // irons_spellbooks:angel_wings エフェクトで飛行ビジュアルを付与
        var effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks", "angel_wings"));
        if (effect != null) {
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect, 20000000, 0, false, false, true));
        }

        // Heal 2 HP every 20 ticks (1 second)
        if (entity.tickCount % 20 == 0) {
            entity.heal(2.0f);
        }

        // Creative Flight
        if (entity instanceof Player player) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide) return;

        // angel_wingsエフェクトを解除
        var effectRemove = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("irons_spellbooks", "angel_wings"));
        if (effectRemove != null) {
            entity.removeEffect(effectRemove);
        }

        // クリエイティブ飛行を解除
        if (entity instanceof Player player) {
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable("item.elementmagicboss.angel_ring.description"));
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}
