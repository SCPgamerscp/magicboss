package com.ecrea.elementmagicboss.spell;

import com.ecrea.elementmagicboss.ElementMagicBossMod;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 倍化 (Multiply)
 * メインハンドとオフハンドのアイテム数量を2倍にする（スタック上限まで）
 */
public class MultiplySpell extends AbstractSpell {

    private final ResourceLocation spellId =
            new ResourceLocation(ElementMagicBossMod.MOD_ID, "multiply");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(5)
            .build();

    public MultiplySpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower   = 1;
        this.castTime         = 0;
        this.baseManaCost     = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.elementmagicboss.multiply_multiplier", getMultiplier(spellLevel))
        );
    }

    @Override public CastType getCastType()              { return CastType.INSTANT; }
    @Override public DefaultConfig getDefaultConfig()    { return defaultConfig; }
    @Override public ResourceLocation getSpellResource() { return spellId; }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity,
                       CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide && entity instanceof Player player) {
            int multiplier = getMultiplier(spellLevel);
            multiplyItem(player, player.getMainHandItem(), multiplier);
            multiplyItem(player, player.getOffhandItem(),  multiplier);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    /**
     * アイテムを multiplier 倍にする。スタック上限・スタッカブル問わず強制倍増。
     * ItemStack.setCount() はgetMaxStackSize()を無視して動作する。
     */
    private void multiplyItem(Player player, ItemStack stack, int multiplier) {
        if (stack.isEmpty()) return;
        stack.setCount(stack.getCount() * multiplier);
    }

    /** レベルに応じた倍率: Lv1=2倍, Lv2=3倍, ... */
    private int getMultiplier(int spellLevel) {
        return 1 + spellLevel;
    }
}
