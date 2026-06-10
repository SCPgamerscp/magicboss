package com.ecrea.elementmagicboss.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeMod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

public class GreatWizardRingItem extends Item implements ICurioItem {
    // UUIDs for attribute modifiers
    private static final UUID MANA_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d801");
    private static final UUID COOLDOWN_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d802");
    private static final UUID CAST_TIME_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d803");
    private static final UUID SPELL_RES_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d804");
    private static final UUID SUMMON_DMG_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d805");
    private static final UUID MANA_REGEN_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d806");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d807");
    private static final UUID ARMOR_UUID = UUID.fromString("c07b301c-6d98-4c07-b089-af0c8976d808");

    public GreatWizardRingItem(Properties pProperties) {
        super(pProperties.rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot pSlot) {
        if (pSlot == net.minecraft.world.entity.EquipmentSlot.OFFHAND || pSlot == net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(pSlot));
            
            // Mana +1000
            builder.put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(MANA_UUID, "Ring Mana", 1000.0, AttributeModifier.Operation.ADDITION));
            
            // Cooldown Reduction +100% (Note: IRON's uses percentage)
            builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(COOLDOWN_UUID, "Ring CDR", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Cast Time Reduction +100%
            builder.put(AttributeRegistry.CAST_TIME_REDUCTION.get(), new AttributeModifier(CAST_TIME_UUID, "Ring Cast Time", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Spell Resistance +100%
            builder.put(AttributeRegistry.SPELL_RESIST.get(), new AttributeModifier(SPELL_RES_UUID, "Ring Spell Res", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Summon Damage +100%
            builder.put(AttributeRegistry.SUMMON_DAMAGE.get(), new AttributeModifier(SUMMON_DMG_UUID, "Ring Summon Dmg", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Mana Regen +100%
            builder.put(AttributeRegistry.MANA_REGEN.get(), new AttributeModifier(MANA_REGEN_UUID, "Ring Mana Regen", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Spell Power +100%
            builder.put(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(SPELL_POWER_UUID, "Ring Spell Power", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Armor +10
            builder.put(Attributes.ARMOR, new AttributeModifier(ARMOR_UUID, "Ring Armor", 10.0, AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(pSlot);
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        
        // Use the UUID provided by Curios to avoid conflicts
        // Mana +1000
        builder.put(AttributeRegistry.MAX_MANA.get(), new AttributeModifier(uuid, "Ring Mana", 1000.0, AttributeModifier.Operation.ADDITION));
        
        // Cooldown Reduction +100%
        builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(), new AttributeModifier(uuid, "Ring CDR", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Cast Time Reduction +100%
        builder.put(AttributeRegistry.CAST_TIME_REDUCTION.get(), new AttributeModifier(uuid, "Ring Cast Time", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Spell Resistance +100%
        builder.put(AttributeRegistry.SPELL_RESIST.get(), new AttributeModifier(uuid, "Ring Spell Res", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Summon Damage +100%
        builder.put(AttributeRegistry.SUMMON_DAMAGE.get(), new AttributeModifier(uuid, "Ring Summon Dmg", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Mana Regen +100%
        builder.put(AttributeRegistry.MANA_REGEN.get(), new AttributeModifier(uuid, "Ring Mana Regen", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Spell Power +100%
        builder.put(AttributeRegistry.SPELL_POWER.get(), new AttributeModifier(uuid, "Ring Spell Power", 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
        
        // Armor +10
        builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Ring Armor", 10.0, AttributeModifier.Operation.ADDITION));

        return builder.build();
    }
}
