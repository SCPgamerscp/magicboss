package com.ecrea.elementmagicboss.accessor;

/**
 * CreeperHeadProjectile に追加する Chain Chain 用の状態設定アクセサ。
 * Mixin パッケージ外に置いて、通常コードから安全に参照できるようにする。
 */
public interface ChainChainAccessor {
    void elementmagicboss$setChainChainData(int remaining, int count, float damage);
}
