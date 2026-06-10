# ⚡ Angel Rasiel 魔法使いアップデート完了！

## 🎉 更新内容

Angel Rasielが**強力な魔法使い**になりました！

### ✨ 主な変更点

1. **AbstractSpellCastingMobを継承**
   - Iron's Spellbooksの魔法が使えるようになりました

2. **9種類の強力な魔法を使用**
   - Divine Smite x7
   - Guiding Bolt x15
   - Sunbeam x12
   - Heal x3
   - Lightning Lance x7
   - Flaming Barrage x15
   - Root x1 → Magma Bomb x15
   - Thunderstorm x1（効果中は再詠唱しない）
   - Ball Lightning x15

3. **エンチャントされたダイヤモンドの剣を装備**
   - ダメージ増加 XX (20)
   - ドロップ増加 XX (20)
   - 火属性 XX (20)

4. **強化されたステータス**
   - 体力: 200（ハート100個）
   - マナ: 50,000
   - クールダウン減少: 100%
   - 詠唱時間短縮: 80%

---

## 📁 変更されたファイル

### 更新（2ファイル）
✅ `src/main/java/com/ecrea/elementmagicboss/entity/AngelRasielEntity.java`
   - AbstractSpellCastingMobを継承
   - 魔法使い用の属性を追加
   - エンチャント剣の装備を実装

✅ `src/main/java/com/ecrea/elementmagicboss/event/ModEventBusEvents.java`
   - スポーン登録をコメントアウト（自然スポーン無効化）

### 新規作成（1ファイル）
✅ `src/main/java/com/ecrea/elementmagicboss/entity/ai/AngelRasielAttackGoal.java`
   - Angel Rasiel専用の魔法攻撃AI
   - 指定された順序で魔法を使用
   - Thunderstormの効果チェック機能

### ドキュメント更新
✅ `ANGEL_RASIEL_README.md`
   - 魔法使いバージョンの詳細説明

---

## 🚀 ビルドとテスト

### 1. テクスチャのコピー（まだの場合）
```cmd
cd C:\Users\ecrea\Downloads\magicboss
copy "angelA羽つき.png" "src\main\resources\assets\elementmagicboss\textures\entity\angel_rasiel.png"
```

### 2. ビルド
```cmd
gradlew clean build
```

### 3. 実行
```cmd
gradlew runClient
```

### 4. ゲーム内でテスト
1. クリエイティブモードで起動
2. 「エレメントマジックボス」タブから「天使ラジエルのスポーンエッグ」を取得
3. 地面に使用してスポーン
4. Angel Rasielが魔法を連続使用するのを確認！

---

## 🎯 魔法の順序

Angel Rasielは以下の順序で魔法を使います：

1. Divine Smite x7 ⚡
2. Guiding Bolt x15 🌟
3. Sunbeam x12 ☀️
4. Heal x3 💚
5. Lightning Lance x7 ⚡
6. Flaming Barrage x15 🔥
7. Root x1 🌿 → Magma Bomb x15 💥
8. Thunderstorm x1 ⛈️（効果中は再詠唱しない）
9. Ball Lightning x15 ⚡

**全91回の魔法攻撃** + 近接攻撃（エンチャント剣）

---

## ⚙️ カスタマイズ

### 魔法の変更
`AngelRasielAttackGoal.java` の `initializeSpellSequence()` で変更：
```java
// 例: Divine Smiteを10回に変更
queueSpell(SpellRegistry.DIVINE_SMITE_SPELL.get(), level, 10);

// 新しい魔法を追加
queueSpell(SpellRegistry.FIREBALL_SPELL.get(), level, 5);
```

### 体力の変更
`AngelRasielEntity.java` の `createAttributes()` で変更：
```java
.add(Attributes.MAX_HEALTH, 200.0D)  // 体力を変更
```

### 剣のエンチャント変更
`AngelRasielEntity.java` の `populateDefaultEquipmentSlots()` で変更：
```java
sword.enchant(Enchantments.SHARPNESS, 20);  // レベルを変更
```

---

## 🎮 戦闘のヒント

### プレイヤー向けのヒント
- **距離を保つ**: 魔法攻撃の射程が長い
- **回復に注意**: Healで3回回復する
- **Root警戒**: 拘束されるとMagma Bombの集中砲火
- **Thunderstorm中**: 周囲が危険
- **剣にも注意**: 近づくとエンチャント剣で攻撃される

### 難易度調整
難しすぎる場合：
- 体力を減らす（200 → 100など）
- 魔法の回数を減らす
- マナやクールダウン減少を下げる
- 剣のエンチャントレベルを下げる

---

## ✅ チェックリスト

セットアップ確認：
- [ ] テクスチャファイルをコピーした
- [ ] ビルドが成功した
- [ ] ゲームが起動した
- [ ] スポーンエッグが表示される
- [ ] Angel Rasielがスポーンする
- [ ] 魔法を使用する
- [ ] エンチャント剣を持っている
- [ ] 翼がアニメーションする

---

## 🐛 トラブルシューティング

### 魔法を使わない
→ Iron's Spellbooksが正しくインストールされているか確認

### コンパイルエラー
→ `gradlew clean` してから再ビルド

### 剣を持っていない
→ スポーン時に装備が生成されるはず。コードを確認

---

## 🎊 完成！

**Angel Rasiel 魔法使いバージョン**が完成しました！

- ✅ 9種類の強力な魔法
- ✅ 合計91回の魔法攻撃
- ✅ エンチャントされたダイヤモンドの剣
- ✅ 翼のアニメーション
- ✅ 自然スポーン無効（テスト中）

ビルドしてゲームで試してみてください！ 🪽✨⚡

詳細は `ANGEL_RASIEL_README.md` を参照してください。
