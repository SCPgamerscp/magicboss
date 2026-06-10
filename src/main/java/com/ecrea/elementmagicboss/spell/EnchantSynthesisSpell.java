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
 * エンチャント合成 (Enchant Synthesis)
 * オフハンドのエンチャント本を消費し、メインハンドのアイテムへエンチャントを合成する。
 * レベル上限・競合チェックを完全に無視して合算する。
 */
public class EnchantSynthesisSpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "enchant_synthesis");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(15)
            .build();

    public EnchantSynthesisSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower   = 1;
        this.castTime         = 0;
        this.baseManaCost     = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.enchant_synthesis_info")
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {

        if (!level.isClientSide && entity instanceof Player player) {
            ItemStack offhand  = player.getOffhandItem();
            ItemStack mainhand = player.getMainHandItem();

            // バリデーション
            if (offhand.isEmpty() || !(offhand.getItem() instanceof EnchantedBookItem)) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_synthesis_need_book"));
                return;
            }
            if (mainhand.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_synthesis_need_item"));
                return;
            }

            // ソース：本の StoredEnchantments を取得
            ListTag bookEnchants = EnchantedBookItem.getEnchantments(offhand);
            if (bookEnchants.isEmpty()) {
                player.sendSystemMessage(
                        Component.translatable("message.elementmagicboss.enchant_synthesis_no_enchant"));
                return;
            }

            // ターゲットタグ名の決定（エンチャント本→StoredEnchantments、それ以外→Enchantments）
            boolean targetIsBook = mainhand.getItem() == Items.ENCHANTED_BOOK
                    || mainhand.getItem() instanceof EnchantedBookItem;
            String targetTagName = targetIsBook ? "StoredEnchantments" : "Enchantments";

            CompoundTag targetNbt = mainhand.getOrCreateTag();

            // 既存エンチャントリストを取得（なければ新規作成してタグに登録）
            ListTag targetEnchants;
            if (targetNbt.contains(targetTagName, 9)) {
                targetEnchants = targetNbt.getList(targetTagName, 10);
            } else {
                targetEnchants = new ListTag();
                targetNbt.put(targetTagName, targetEnchants);
            }

            // エンチャントを合算マージ（上限・競合チェックなし）
            for (int i = 0; i < bookEnchants.size(); i++) {
                CompoundTag bookEntry = bookEnchants.getCompound(i);
                String enchantId = bookEntry.getString("id");
                int addLevel = bookEntry.getShort("lvl");

                boolean found = false;
                for (int j = 0; j < targetEnchants.size(); j++) {
                    CompoundTag targetEntry = targetEnchants.getCompound(j);
                    if (targetEntry.getString("id").equals(enchantId)) {
                        int currentLevel = targetEntry.getShort("lvl");
                        targetEntry.putShort("lvl", (short) (currentLevel + addLevel));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    targetEnchants.add(bookEntry.copy());
                }
            }

            mainhand.setTag(targetNbt);
            offhand.shrink(1);

            player.sendSystemMessage(
                    Component.translatable("message.elementmagicboss.enchant_synthesis_success"));
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
