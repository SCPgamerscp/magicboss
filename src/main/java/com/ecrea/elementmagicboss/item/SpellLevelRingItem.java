package com.ecrea.elementmagicboss.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

/**
 * 魔法レベルリング
 * 装備中、全ての魔法のレベルを+10し、最大マナを+5000する。
 * Curiosのスロット固有UUIDを使うため、複数装備で効果が重複する。
 */
public class SpellLevelRingItem extends Item implements ICurioItem {

    public SpellLevelRingItem() {
        super(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        // uuid はCuriosがスロットごとに生成する固有UUID → 複数装備で重複加算される
        builder.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(uuid, "Spell Level Ring Mana", 5000.0, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
