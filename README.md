# CustomizeFishing

Minecraft 1.20の釣りシステム拡張プラグイン

## 機能

### 🎣 カスタムルートテーブル

- 15種類のティアシステム（Junk、Fish、Treasure、Common、Rare、Epic、Legendary、Exotic、God など）
- 各ティアごとに数百種類のカスタムアイテム
- 条件に応じたドロップ確率

### 🍀 幸運システム

- 宝釣りエンチャント（最大レベル127対応）
- 幸運/不幸ポーション効果
- 装備による幸運値補正
- 経験値レベルボーナス
- 天気ボーナス（晴れ/雨/雷雨）
- タイミングボーナス（JUST/PERFECT/GREAT/GOOD/MISS）

### ⚡ 特殊機能

- **ダブルフィッシング**: 宝釣りLv10以上＋コンジットパワーLv2以上で2回同時釣り
- **イルカの好意**: 開水域＋イルカの好意効果で特別なアイテムドロップ
- **プレイヤーヘッド**: カスタムプレイヤーヘッド対応
- **デバッグモード**: 詳細ログ出力

### 🎯 タイミングシステム

釣り上げのタイミングに応じたボーナス：

- **JUST** (50ms以内): +0.8% 幸運ボーナス
- **PERFECT** (100ms以内): +0.64% 幸運ボーナス
- **GREAT** (300ms以内): +0.48% 幸運ボーナス
- **GOOD** (750ms以内): +0.32% 幸運ボーナス
- **MISS** (750ms超過): ボーナスなし

## インストール

1. [Spigot 1.20.1](https://hub.spigotmc.org/nexus/content/repositories/snapshots/)サーバーをセットアップ
2. ビルドされたJARファイルを`plugins`フォルダに配置
3. サーバーを起動

## ビルド方法

```bash
# プロジェクトのクローン
git clone https://github.com/wax100/CustomizeFishing.git
cd CustomizeFishing

# ビルド
./gradlew build

# テストサーバーの起動（開発用）
./gradlew runServer
```

## コマンド

| コマンド                         | 説明          | 権限                          |
|------------------------------|-------------|-----------------------------|
| `/customizefishing reload`   | 設定をリロード     | `customizefishing.reload`   |
| `/customizefishing debugrod` | デバッグ用釣り竿を取得 | `customizefishing.debugrod` |
| `/customizefishing help`     | ヘルプを表示      | `customizefishing.use`      |

エイリアス: `/cf`, `/cfish`

## 権限

| 権限                          | 説明         | デフォルト |
|-----------------------------|------------|-------|
| `customizefishing.use`      | 基本コマンドの使用  | 全員    |
| `customizefishing.reload`   | 設定のリロード    | OP    |
| `customizefishing.debugrod` | デバッグ釣り竿の取得 | OP    |
| `customizefishing.*`        | 全権限        | OP    |

## 設定ファイル

`config.yml`で以下の設定が可能：

- デバッグモードの有効/無効
- カスタムルートテーブルの調整
- 確率計算の詳細設定

## データパック

`data/customize_fishing/loot_tables/gameplay/fishing/`に各ティアのルートテーブルが定義されています：

- `junk.json` - ガラクタアイテム
- `fish.json` - 魚類
- `treasure.json` - 宝物
- `common.json` - 一般アイテム
- `rare.json` - レアアイテム
- `epic.json` - エピックアイテム
- `legendary.json` - レジェンダリーアイテム
- `exotic.json` - エキゾチックアイテム
- `god.json` - ゴッドティアアイテム
- その他特殊ティア

## 必要環境

- Java 17以上
- Gradle 7.0以上
- Spigot API 1.20.1

### プロジェクト構造

```
src/main/java/io/wax100/customizeFishing/
├── CustomizeFishing.java        # メインクラス
├── commands/                    # コマンド処理
├── debug/                       # デバッグ機能
├── effects/                     # エフェクト処理
├── enums/                       # 列挙型定義
├── fishing/                     # 釣りロジック
├── listeners/                   # イベントリスナー
├── luck/                        # 幸運計算
└── timing/                      # タイミング判定
```

## 作者

wax100