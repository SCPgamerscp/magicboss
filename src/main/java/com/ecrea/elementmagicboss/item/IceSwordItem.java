package com.ecrea.elementmagicboss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public class IceSwordItem extends SwordItem {
    
    // カスタムティア: 攻撃ダメージ10、攻撃速度3
    private static final Tier ICE_SWORD_TIER = new Tier() {
        @Override
        public int getUses() {
            return -1; // 耐久値無限（実際には減らない）
        }

        @Override
        public float getSpeed() {
            return 12.0f; // 採掘速度
        }

        @Override
        public float getAttackDamageBonus() {
            return 9.0f; // プレイヤーの基礎攻撃力1 + ティアボーナス9 = 合計攻撃力10
        }

        @Override
        public int getLevel() {
            return 4; // ダイヤモンドレベル
        }

        @Override
        public int getEnchantmentValue() {
            return 20;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    public IceSwordItem(Properties properties) {
        // 攻撃速度を3にするため、-1.0fを指定（バニラの基本値4 - 1 = 3）
        // 第2引数は0（Tierのボーナスのみ使用）
        super(ICE_SWORD_TIER, 0, -1.0f, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // 2147483647tick（約24855日）の凍結効果を付与
        // 攻撃するたびに効果がリセットされる
        
        // Minecraftの凍結状態を直接設定（パウダースノーと同じ効果）
        target.setTicksFrozen(2147483647);
        
        // 耐久値を減らさない
        return true;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false; // 耐久値が減らない
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // エンチャント光沢を付ける
    }
}