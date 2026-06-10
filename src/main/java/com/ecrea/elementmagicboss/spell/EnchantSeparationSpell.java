package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * エンチャント分離 (Enchant Separation)
 * メインハンドのエンチャント済みアイテムからエンチャントをすべて取り出し、
 * エンチャント本としてオフハンドに生成する。
 * メインハンドのアイテムからはエンチャントが除去される。
 */
public class EnchantSeparationSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "enchant_separation");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(15)
            .build();

    public EnchantSeparationSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower   = 1;
        this.castTime         = 0;
        this.baseManaCost     = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.enchant_separation_info")
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        if (!level.isClientSide && entity instanceof Player player) {
            ItemStack mainhand = player.getMainHandItem();
            ItemStack offhand  = player.getOffhandItem();

            // バリデーション：メインハンドが空
            if (mainhand.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_separation_need_item"));
                return;
            }

            // バリデーション：オフハンドが空でない
            if (!offhand.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_separation_need_empty_offhand"));
                return;
            }

            // エンチャントを取得（エンチャント本の場合は StoredEnchantments、それ以外は Enchantments）
            CompoundTag nbt = mainhand.getTag();
            if (nbt == null) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_separation_no_enchant"));
                return;
            }

            boolean sourceIsBook = mainhand.getItem() instanceof EnchantedBookItem;
            String sourceTagName = sourceIsBook ? "StoredEnchantments" : "Enchantments";

            if (!nbt.contains(sourceTagName, 9)) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_separation_no_enchant"));
                return;
            }

            ListTag enchants = nbt.getList(sourceTagName, 10);
            if (enchants.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_separation_no_enchant"));
                return;
            }

            // エンチャント本を生成し、StoredEnchantments にコピー
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            CompoundTag bookNbt = book.getOrCreateTag();
            bookNbt.put("StoredEnchantments", enchants.copy());
            book.setTag(bookNbt);

            // メインハンドからエンチャントを除去
            nbt.remove(sourceTagName);
            // エンチャント本だった場合、エンチャントが空になったら通常の本に戻す
            if (sourceIsBook) {
                mainhand = new ItemStack(Items.BOOK);
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, mainhand);
            } else {
                mainhand.setTag(nbt);
            }

            // オフハンドにエンチャント本を配置
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, book);

            player.sendSystemMessage(
                    Component.translatable("message.elementmagicboss.enchant_separation_success"));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
