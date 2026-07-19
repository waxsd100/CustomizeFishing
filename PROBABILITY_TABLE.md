# CustomizeFishing 確率表

## カテゴリ一覧

| カテゴリ | レア度名 | 優先度 | 基本確率 | Quality | 条件 | 天気 |
|---|---|---|---|---|---|---|
| dolphins_grace | - | 0 | 100.0% | 0 | 開水域＋イルカの好意 | 晴れ |
| unique | - | 1 | 0.00000005% | 0 | 開水域＋宝釣りLv10＋総幸運22以上 | 雨/雷 |
| god | God | 2 | 0.000005% | 0 | 開水域＋宝釣りLv10＋総幸運14以上 | 雷 |
| cosmic | コズミック / Cosmic | 3 | 0.0001% | 0 | 開水域＋宝釣りLv10＋総幸運20以上 | 雷 |
| divine | ディバイン / Divine | 4 | 0.001% | 0 | 開水域＋宝釣りLv10＋総幸運18以上 | 雨/雷 |
| celestial | セレスティアル / Celestial | 5 | 0.01% | 0.5 | 開水域＋宝釣りLv6＋総幸運16以上 | 雨/雷 |
| beyond | ビヨンド / Beyond | 6 | 0.05% | 0.5 | 開水域＋宝釣りLv5＋総幸運12以上 | 全天候 |
| arcana | アルカナ / Arcana | 7 | 0.15% | 1 | 開水域＋宝釣りLv5＋総幸運10以上 | 全天候 |
| immortal | イモータル / Immortal | 8 | 0.2% | 1 | 開水域＋宝釣りLv4＋総幸運8以上 | 全天候 |
| legendary | レジェンダリー / Legendary | 9 | 2.0% | 2 | 開水域＋宝釣りLv4＋総幸運5以上 | 全天候 |
| rare | レア / Rare | 10 | 10.0% | 3 | 開水域＋宝釣りLv3＋総幸運4以上 | 全天候 |
| uncommon | アンコモン / Uncommon | 11 | 15.0% | 4 | 開水域＋宝釣りLv3＋総幸運2以上 | 全天候 |
| common | コモン / Common | 12 | 30.0% | 5 | 開水域 | 全天候 |

- 「総幸運◯以上」はカテゴリ条件の `min_total_luck`。総幸運値（後述）がこの値未満だと抽選対象外。
- `min_total_luck` が1以上のカテゴリは、解禁直後は重みが抑制され、しきい値をどれだけ超えたかに応じて満額に近づく（しきい値ランプ、後述）。
- どのカテゴリにも当選しなかった場合はバニラの釣り結果がそのまま使用される（treasure / fish / junk ティアは廃止済み）。
- 各ティアには自ティアのテーブルを10回抽選する「10連ガチャボックス」（シュルカーボックス）が含まれる。

# アイテム詳細確率表

## COMMON（コモン）ティア

**合計ウェイト: 348**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| 古い釣り竿 | 18 | 5.17% | 1 |
| minecraft:emerald_block | 10 | 2.87% | 2-5 |
| minecraft:coal_ore | 35 | 10.06% | 1-5 |
| minecraft:copper_ore | 30 | 8.62% | 1-4 |
| minecraft:iron_ore | 15 | 4.31% | 1-3 |
| ボロい釣り竿 | 15 | 4.31% | 1 |
| すごくボロい釣り竿 | 12 | 3.45% | 1 |
| 壊れかけの弓 | 5 | 1.44% | 1 |
| 古びた魔法書 | 3 | 0.86% | 1 |
| 古い革 | 20 | 5.75% | 1-2 |
| 壊れた道具 | 15 | 4.31% | 1 |
| 泥 | 18 | 5.17% | 2-4 |
| 水入りポーション | 12 | 3.45% | 1 |
| 空のポーション | 8 | 2.30% | 1 |
| 失敗ポーション | 5 | 1.44% | 1 |
| minecraft:emerald_block | 5 | 1.44% | 2-3 |
| minecraft:coal_block | 15 | 4.31% | 1-3 |
| minecraft:iron_block | 10 | 2.87% | 1-2 |
| minecraft:copper_block | 12 | 3.45% | 1-3 |
| minecraft:redstone_block | 10 | 2.87% | 1-2 |
| minecraft:lapis_block | 8 | 2.30% | 1-2 |
| minecraft:nether_bricks | 8 | 2.30% | 2-5 |
| minecraft:magma_block | 8 | 2.30% | 2-5 |
| ボロすきがき釣りセット | 2 | 0.57% | 1 |
| ボロすきがき釣りセット | 2 | 0.57% | 1 |
| ボロすきがき釣りセット | 2 | 0.57% | 1 |
| ボロすきがき釣りセット | 2 | 0.57% | 1 |
| 10連ガチャボックス（COMMON） | 1 | 0.29% | 1 |
| 駆け出しの釣り竿 | 10 | 2.87% | 1 |
| すきがきの怪しい試飲品 | 6 | 1.72% | 1 |
| すきがきの残飯スープ | 6 | 1.72% | 1 |
| すきがきのがぶ飲み井戸水 | 4 | 1.15% | 1 |
| すきがきの飛び散る残飯 | 4 | 1.15% | 1 |
| すきがきの継ぎ接ぎ釣り竿 | 6 | 1.72% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.29% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.29% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.29% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.29% | 1 |
| SKGKYRの頭 | 2 | 0.57% | 1 |

## UNCOMMON（アンコモン）ティア

**合計ウェイト: 196**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| 普通の釣り竿 | 14 | 7.14% | 1 |
| すきがき釣りセット | 1 | 0.51% | 1 |
| すきがき釣りセット | 1 | 0.51% | 1 |
| すきがき釣りセット | 1 | 0.51% | 1 |
| すきがき釣りセット | 1 | 0.51% | 1 |
| minecraft:emerald_block | 8 | 4.08% | 5-12 |
| minecraft:iron_ore | 12 | 6.12% | 1-2 |
| minecraft:copper_ore | 10 | 5.10% | 1-3 |
| minecraft:coal_ore | 8 | 4.08% | 2-4 |
| 普通の剣 | 10 | 5.10% | 1 |
| 普通の弓 | 10 | 5.10% | 1 |
| 普通のクロスボウ | 8 | 4.08% | 1 |
| 錆びたトライデント | 3 | 1.53% | 1 |
| まあまあの釣り竿 | 10 | 5.10% | 1 |
| ちょっといい魔法書 | 8 | 4.08% | 1 |
| 普通の盾 | 6 | 3.06% | 1 |
| 経験値のビン | 10 | 5.10% | 3-8 |
| minecraft:golden_apple | 5 | 2.55% | 1-2 |
| 10連ガチャボックス（UNCOMMON） | 1 | 0.51% | 1 |
| minecraft:gold_ore | 8 | 4.08% | 1-3 |
| minecraft:emerald_ore | 6 | 3.06% | 1-2 |
| minecraft:gold_block | 5 | 2.55% | 1-2 |
| minecraft:amethyst_block | 6 | 3.06% | 1-3 |
| minecraft:purpur_block | 4 | 2.04% | 2-5 |
| minecraft:end_stone | 5 | 2.55% | 2-5 |
| artifacts:whoopee_cushion | 1 | 0.51% | 1 |
| artifacts:rooted_boots | 1 | 0.51% | 1 |
| artifacts:scarf_of_invisibility | 1 | 0.51% | 1 |
| artifacts:snowshoes | 1 | 0.51% | 1 |
| artifacts:charm_of_sinking | 1 | 0.51% | 1 |
| 愛用の釣り竿 | 6 | 3.06% | 1 |
| すきがき果実のラッキージュース | 5 | 2.55% | 1 |
| すきがきの闇鍋エキス | 3 | 1.53% | 1 |
| すきがきのはじけるサイダー | 4 | 2.04% | 1 |
| すきがきのこぼれ闇鍋 | 2 | 1.02% | 1 |
| すきがき手作り釣り竿 | 4 | 2.04% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.51% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.51% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.51% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.51% | 1 |
| SKGKYRの頭 | 2 | 1.02% | 1 |

## RARE（レア）ティア

**合計ウェイト: 403**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| minecraft:slime_ball | 1 | 0.25% | 1 |
| minecraft:kelp | 1 | 0.25% | 1 |
| minecraft:book | 1 | 0.25% | 1 |
| minecraft:spawner | 1 | 0.25% | 1 |
| minecraft:zombie_head | 1 | 0.25% | 1 |
| minecraft:book | 1 | 0.25% | 1 |
| minecraft:ink_sac | 1 | 0.25% | 1-2 |
| minecraft:glow_ink_sac | 1 | 0.25% | 1-2 |
| minecraft:lily_pad | 1 | 0.25% | 2-3 |
| minecraft:potion | 1 | 0.25% | 1 |
| minecraft:leather_boots | 1 | 0.25% | 1 |
| minecraft:skeleton_skull | 1 | 0.25% | 1 |
| minecraft:frogspawn | 1 | 0.25% | 1-3 |
| minecraft:player_head | 1 | 0.25% | 1 |
| minecraft:music_disc_mall | 1 | 0.25% | 1 |
| minecraft:music_disc_mellohi | 1 | 0.25% | 1 |
| minecraft:music_disc_stal | 1 | 0.25% | 1 |
| minecraft:music_disc_ward | 1 | 0.25% | 1 |
| minecraft:music_disc_11 | 1 | 0.25% | 1 |
| minecraft:music_disc_wait | 1 | 0.25% | 1 |
| minecraft:music_disc_otherside | 1 | 0.25% | 1 |
| minecraft:music_disc_5 | 1 | 0.25% | 1 |
| minecraft:bow | 1 | 0.25% | 1 |
| minecraft:diamond | 1 | 0.25% | 1-2 |
| minecraft:emerald_block | 1 | 0.25% | 1 |
| minecraft:diamond_block | 1 | 0.25% | 1 |
| minecraft:diamond_ore | 2 | 0.50% | 1-3 |
| minecraft:amethyst_shard | 1 | 0.25% | 1-4 |
| ちょっと良い釣り竿 | 1 | 0.25% | 1 |
| 良い釣り竿 | 1 | 0.25% | 1 |
| 優秀な戦闘弓 | 1 | 0.25% | 1 |
| 稀少な魔法書 | 1 | 0.25% | 1 |
| すきがき普段着セット | 1 | 0.25% | 1 |
| すきがき普段着セット | 1 | 0.25% | 1 |
| すきがき普段着セット | 1 | 0.25% | 1 |
| minecraft:trident | 9 | 2.23% | 1 |
| すきがき普段着セット | 1 | 0.25% | 1 |
| minecraft:emerald_block | 15 | 3.72% | 18-50 |
| すきがき釣りセット | 1 | 0.25% | 1 |
| すきがき釣りセット | 1 | 0.25% | 1 |
| すきがき釣りセット | 1 | 0.25% | 1 |
| すきがき釣りセット | 1 | 0.25% | 1 |
| ◦ハニカム-D-デイリー◦ | 5 | 1.24% | 1 |
| ●ハニカム-N-ナイト● | 3 | 0.74% | 1 |
| minecraft:raw_copper | 6 | 1.49% | 1-2 |
| minecraft:raw_iron | 8 | 1.99% | 1-3 |
| minecraft:gold_nugget | 4 | 0.99% | 2-5 |
| minecraft:name_tag | 1 | 0.25% | 1 |
| minecraft:saddle | 1 | 0.25% | 1 |
| minecraft:phantom_membrane | 7 | 1.74% | 1 |
| minecraft:rabbit_hide | 10 | 2.48% | 1 |
| minecraft:bow | 1 | 0.25% | 1 |
| minecraft:book | 1 | 0.25% | 1 |
| minecraft:nautilus_shell | 1 | 0.25% | 1 |
| ボロ釣り竿 | 8 | 1.99% | 1 |
| 普通の釣り竿 | 2 | 0.50% | 1 |
| ボロ釣り竿 | 8 | 1.99% | 1 |
| ボロい修繕釣り竿 | 1 | 0.25% | 1 |
| minecraft:axolotl_bucket | 3 | 0.74% | 1 |
| minecraft:tadpole_bucket | 3 | 0.74% | 1 |
| minecraft:raw_copper | 33 | 8.19% | 1 |
| minecraft:gold_nugget | 10 | 2.48% | 1 |
| minecraft:raw_iron | 37 | 9.18% | 1 |
| minecraft:emerald_block | 15 | 3.72% | 30-68 |
| minecraft:redstone | 35 | 8.68% | 1-4 |
| minecraft:lapis_lazuli | 31 | 7.69% | 1-3 |
| minecraft:emerald_block | 20 | 4.96% | 45-105 |
| quark:bottled_cloud | 1 | 0.25% | 1 |
| quark:crab_bucket | 1 | 0.25% | 1 |
| quark:gold_bars | 1 | 0.25% | 1 |
| quark:rope | 1 | 0.25% | 1 |
| quark:seed_pouch | 1 | 0.25% | 1 |
| quark:trowel | 1 | 0.25% | 1 |
| そろばん | 1 | 0.25% | 1 |
| |||呪われた釣り竿||| | 2 | 0.50% | 1 |
| 呪われた漁師の帽子 | 2 | 0.50% | 1 |
| quark:bottled_cloud | 1 | 0.25% | 1 |
| quark:crab_bucket | 1 | 0.25% | 1 |
| quark:gold_bars | 1 | 0.25% | 1 |
| quark:ravager_hide | 1 | 0.25% | 1 |
| quark:rope | 1 | 0.25% | 1 |
| quark:seed_pouch | 1 | 0.25% | 1 |
| quark:trowel | 1 | 0.25% | 1 |
| 海賊のさびた剣 | 5 | 1.24% | 1 |
| 水浸しの長靴 | 5 | 1.24% | 1 |
| 河童の皿 | 5 | 1.24% | 1 |
| 流木で作った盾 | 5 | 1.24% | 1 |
| すきがきの銛 | 5 | 1.24% | 1 |
| 期限切れの栄養ドリンク | 5 | 1.24% | 1 |
| フジツボだらけの胸当て | 5 | 1.24% | 1 |
| 10連ガチャボックス（RARE） | 1 | 0.25% | 1 |
| artifacts:anglers_hat | 1 | 0.25% | 1 |
| artifacts:cowboy_hat | 1 | 0.25% | 1 |
| artifacts:onion_ring | 1 | 0.25% | 1 |
| artifacts:novelty_drinking_hat | 1 | 0.25% | 1 |
| artifacts:plastic_drinking_hat | 1 | 0.25% | 1 |
| artifacts:pickaxe_heater | 1 | 0.25% | 1 |
| 腕利きの釣り竿 | 4 | 0.99% | 1 |
| すきがき醸造の幸運酒 | 4 | 0.99% | 1 |
| すきがきの呪い水 | 3 | 0.74% | 1 |
| すきがき勝負前の景気付け | 3 | 0.74% | 1 |
| すきがきの撒き酒 | 2 | 0.50% | 1 |
| すきがき工房の釣り竿 | 3 | 0.74% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.25% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.25% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.25% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.25% | 1 |
| SKGKYRの頭 | 1 | 0.25% | 1 |

## LEGENDARY（レジェンダリー）ティア

**合計ウェイト: 171**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| minecraft:beacon | 1 | 0.58% | 1 |
| minecraft:totem_of_undying | 15 | 8.77% | 1-2 |
| minecraft:elytra | 1 | 0.58% | 1 |
| minecraft:book | 1 | 0.58% | 1 |
| 準伝説釣り竿 | 1 | 0.58% | 1 |
| 伝説釣り竿? | 1 | 0.58% | 1 |
| |||伝説釣り竿||| | 1 | 0.58% | 1 |
| 伝説の神弓 | 1 | 0.58% | 1 |
| minecraft:netherrack | 1 | 0.58% | 5-12 |
| minecraft:soul_soil | 1 | 0.58% | 4-8 |
| minecraft:crimson_stem | 1 | 0.58% | 2-6 |
| minecraft:warped_stem | 1 | 0.58% | 2-6 |
| minecraft:crimson_nylium | 1 | 0.58% | 2-5 |
| minecraft:warped_nylium | 1 | 0.58% | 2-5 |
| minecraft:ghast_tear | 1 | 0.58% | 1-3 |
| minecraft:ancient_debris | 1 | 0.58% | 1-2 |
| minecraft:netherite_scrap | 1 | 0.58% | 1-3 |
| minecraft:amethyst_cluster | 1 | 0.58% | 1-2 |
| minecraft:large_amethyst_bud | 1 | 0.58% | 1-3 |
| minecraft:quartz | 1 | 0.58% | 2-5 |
| minecraft:blackstone | 1 | 0.58% | 4-10 |
| minecraft:basalt | 1 | 0.58% | 4-10 |
| minecraft:ender_chest | 1 | 0.58% | 1-2 |
| すきがき戦闘セット | 1 | 0.58% | 1 |
| minecraft:emerald_block | 35 | 20.47% | 27-53 |
| すきがき釣りセット | 1 | 0.58% | 1 |
| すきがき釣りセット | 1 | 0.58% | 1 |
| すきがき釣りセット | 1 | 0.58% | 1 |
| すきがき釣りセット | 1 | 0.58% | 1 |
| ?ハニカム-W-スタンダード? | 3 | 1.75% | 1 |
| !ハニカム-Y-アンスタンダード! | 3 | 1.75% | 1 |
| minecraft:iron_ingot | 5 | 2.92% | 2-5 |
| minecraft:copper_ingot | 8 | 4.68% | 2-6 |
| minecraft:gold_ingot | 4 | 2.34% | 1-4 |
| minecraft:netherrack | 4 | 2.34% | 2-6 |
| minecraft:blackstone | 3 | 1.75% | 1-3 |
| minecraft:guardian_spawn_egg | 1 | 0.58% | 1 |
| minecraft:drowned_spawn_egg | 1 | 0.58% | 1 |
| minecraft:squid_spawn_egg | 1 | 0.58% | 1 |
| minecraft:glow_squid_spawn_egg | 1 | 0.58% | 1 |
| minecraft:axolotl_spawn_egg | 1 | 0.58% | 1 |
| minecraft:turtle_spawn_egg | 1 | 0.58% | 1 |
| minecraft:slime_spawn_egg | 1 | 0.58% | 1 |
| minecraft:enderman_spawn_egg | 1 | 0.58% | 1 |
| minecraft:shulker_spawn_egg | 1 | 0.58% | 1 |
| minecraft:piglin_spawn_egg | 1 | 0.58% | 1-2 |
| minecraft:zombified_piglin_spawn_egg | 1 | 0.58% | 1-2 |
| minecraft:blaze_spawn_egg | 1 | 0.58% | 1 |
| minecraft:ghast_spawn_egg | 1 | 0.58% | 1 |
| minecraft:netherite_ingot | 8 | 4.68% | 1-2 |
| minecraft:netherite_scrap | 6 | 3.51% | 2-4 |
| quark:flamerang | 1 | 0.58% | 1 |
| quark:pickarang | 1 | 0.58% | 1 |
| |=|伝説のフラメラン|=| | 1 | 0.58% | 1 |
| |=|伝説のピッケラン|=| | 1 | 0.58% | 1 |
| 黄金のマグロ包丁 | 3 | 1.75% | 1 |
| すきがきの特製カッパ | 3 | 1.75% | 1 |
| よく効くエナジードリンク | 3 | 1.75% | 1 |
| |||伝説のポンディ||| | 1 | 0.58% | 1 |
| |||伝説?のポンディ||| | 1 | 0.58% | 1 |
| 10連ガチャボックス（LEGENDARY） | 1 | 0.58% | 1 |
| artifacts:panic_necklace | 1 | 0.58% | 1 |
| artifacts:umbrella | 1 | 0.58% | 1 |
| artifacts:everlasting_beef | 1 | 0.58% | 1 |
| artifacts:golden_hook | 1 | 0.58% | 1 |
| artifacts:kitty_slippers | 1 | 0.58% | 1 |
| artifacts:villager_hat | 1 | 0.58% | 1 |
| 英雄の釣り竿 | 3 | 1.75% | 1 |
| すきがき秘蔵の幸運酒 | 3 | 1.75% | 1 |
| すきがき祝いの樽酒 | 2 | 1.17% | 1 |
| すきがき自慢の釣り竿 | 2 | 1.17% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.58% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.58% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.58% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.58% | 1 |
| SKGKYRの頭 | 1 | 0.58% | 1 |

## IMMORTAL（イモータル）ティア

**合計ウェイト: 145**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| minecraft:elytra | 1 | 0.69% | 1 |
| すごく良い釣り竿 | 9 | 6.21% | 1 |
| かなり良い釣り竿 | 1 | 0.69% | 1 |
| イモータルボウ | 1 | 0.69% | 1 |
| イモータル魔法書 | 1 | 0.69% | 1 |
| minecraft:sentry_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:vex_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:wild_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:coast_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:dune_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:wayfinder_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:raiser_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:shaper_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:host_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:ward_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:silence_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:tide_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:snout_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:rib_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:eye_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:spire_armor_trim_smithing_template | 1 | 0.69% | 1 |
| minecraft:experience_bottle | 9 | 6.21% | 1-32 |
| すきがき戦闘セット | 1 | 0.69% | 1 |
| minecraft:emerald_block | 30 | 20.69% | 18-45 |
| すきがき釣りセット | 1 | 0.69% | 1 |
| すきがき釣りセット | 1 | 0.69% | 1 |
| すきがき釣りセット | 1 | 0.69% | 1 |
| すきがき釣りセット | 1 | 0.69% | 1 |
| ?ハニカム-O-ライト? | 1 | 0.69% | 1 |
| !ハニカム-X-ダーク! | 1 | 0.69% | 1 |
| minecraft:wither_skeleton_spawn_egg | 1 | 0.69% | 1 |
| minecraft:elder_guardian_spawn_egg | 1 | 0.69% | 1 |
| minecraft:evoker_spawn_egg | 1 | 0.69% | 1 |
| minecraft:ancient_debris | 1 | 0.69% | 1-2 |
| minecraft:netherite_scrap | 1 | 0.69% | 1-3 |
| minecraft:iron_ingot | 5 | 3.45% | 2-5 |
| minecraft:copper_ingot | 8 | 5.52% | 2-6 |
| minecraft:gold_ingot | 4 | 2.76% | 1-4 |
| quark:ancient_tome（aqua_affinity） | 1 | 0.69% | 1 |
| quark:ancient_tome（blast_protection） | 1 | 0.69% | 1 |
| quark:ancient_tome（depth_strider） | 1 | 0.69% | 1 |
| quark:ancient_tome（efficiency） | 1 | 0.69% | 1 |
| quark:ancient_tome（feather_falling） | 1 | 0.69% | 1 |
| quark:ancient_tome（fire_protection） | 1 | 0.69% | 1 |
| quark:ancient_tome（fortune） | 1 | 0.69% | 1 |
| quark:ancient_tome（frost_walker） | 1 | 0.69% | 1 |
| quark:ancient_tome（looting） | 1 | 0.69% | 1 |
| quark:ancient_tome（luck_of_the_sea） | 1 | 0.69% | 1 |
| quark:ancient_tome（lure） | 1 | 0.69% | 1 |
| quark:ancient_tome（mending） | 1 | 0.69% | 1 |
| quark:ancient_tome（power） | 1 | 0.69% | 1 |
| quark:ancient_tome（projectile_protection） | 1 | 0.69% | 1 |
| quark:ancient_tome（protection） | 1 | 0.69% | 1 |
| quark:ancient_tome（respiration） | 1 | 0.69% | 1 |
| quark:ancient_tome（sharpness） | 1 | 0.69% | 1 |
| quark:ancient_tome（silk_touch） | 1 | 0.69% | 1 |
| quark:ancient_tome（thorns） | 1 | 0.69% | 1 |
| quark:ancient_tome（unbreaking） | 1 | 0.69% | 1 |
| すきがきの炎剣 | 2 | 1.38% | 1 |
| 深海用ヘビメタヘルメット | 2 | 1.38% | 1 |
| すきがき特製プロテイン | 2 | 1.38% | 1 |
| 10連ガチャボックス（IMMORTAL） | 1 | 0.69% | 1 |
| artifacts:running_shoes | 1 | 0.69% | 1 |
| artifacts:bunny_hoppers | 1 | 0.69% | 1 |
| artifacts:flippers | 1 | 0.69% | 1 |
| artifacts:flame_pendant | 1 | 0.69% | 1 |
| artifacts:shock_pendant | 1 | 0.69% | 1 |
| artifacts:thorn_pendant | 1 | 0.69% | 1 |
| artifacts:superstitious_hat | 1 | 0.69% | 1 |
| 不滅の釣り竿 | 3 | 2.07% | 1 |
| すきがき不滅の霊薬 | 2 | 1.38% | 1 |
| すきがき不滅の霊薬・撒布用 | 1 | 0.69% | 1 |
| すきがき亡者の涙 | 1 | 0.69% | 1 |
| すきがき鍛えの釣り竿 | 2 | 1.38% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.69% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.69% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.69% | 1 |
| 呪われたすきがき釣りセット | 1 | 0.69% | 1 |
| SKGKYRの頭 | 1 | 0.69% | 1 |

## ARCANA（アルカナ）ティア

**合計ウェイト: 61**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| すきがきパジャマセット | 1 | 1.64% | 1 |
| すきがきパジャマセット | 1 | 1.64% | 1 |
| すきがきパジャマセット | 1 | 1.64% | 1 |
| すきがきパジャマセット | 1 | 1.64% | 1 |
| minecraft:shulker_shell | 1 | 1.64% | 2-4 |
| minecraft:chorus_fruit | 1 | 1.64% | 4-8 |
| minecraft:chorus_flower | 1 | 1.64% | 1-3 |
| minecraft:end_crystal | 1 | 1.64% | 1 |
| minecraft:shulker_box | 1 | 1.64% | 1 |
| minecraft:dragon_breath | 1 | 1.64% | 2-5 |
| minecraft:netherite_upgrade_smithing_template | 1 | 1.64% | 1 |
| ◆すきがき戦闘セット◆ | 1 | 1.64% | 1 |
| minecraft:emerald_block | 20 | 32.79% | 53-90 |
| 名人の釣り竿 | 1 | 1.64% | 1 |
| すきがき釣りセット | 1 | 1.64% | 1 |
| すきがき釣りセット | 1 | 1.64% | 1 |
| すきがき釣りセット | 1 | 1.64% | 1 |
| すきがき釣りセット | 1 | 1.64% | 1 |
| ?ハニカム-T-クリティカル? | 1 | 1.64% | 1 |
| !ハニカム-Z-ファンブル! | 1 | 1.64% | 1 |
| すきがきの魚群探知機 | 2 | 3.28% | 1 |
| 門外不出の釣りマニュアル | 2 | 3.28% | 1 |
| 高級昆布のウェットスーツ | 2 | 3.28% | 1 |
| 10連ガチャボックス（ARCANA） | 1 | 1.64% | 1 |
| artifacts:digging_claws | 1 | 1.64% | 1 |
| artifacts:feral_claws | 1 | 1.64% | 1 |
| artifacts:power_glove | 1 | 1.64% | 1 |
| artifacts:pocket_piston | 1 | 1.64% | 1 |
| artifacts:antidote_vessel | 1 | 1.64% | 1 |
| artifacts:steadfast_spikes | 1 | 1.64% | 1 |
| 魔導の釣り竿 | 1 | 1.64% | 1 |
| すきがき魔導の福音水 | 1 | 1.64% | 1 |
| すきがき魔導の霧 | 1 | 1.64% | 1 |
| すきがき魔改造釣り竿 | 1 | 1.64% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.64% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.64% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.64% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.64% | 1 |
| SKGKYRの頭 | 1 | 1.64% | 1 |

## BEYOND（ビヨンド）ティア

**合計ウェイト: 66**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| minecraft:dragon_egg | 1 | 1.52% | 1 |
| minecraft:dragon_head | 1 | 1.52% | 1-2 |
| すきがき戦闘服セット | 1 | 1.52% | 1 |
| すきがき戦闘服セット | 1 | 1.52% | 1 |
| すきがき戦闘服セット | 1 | 1.52% | 1 |
| すきがき戦闘服セット | 1 | 1.52% | 1 |
| すきがきパジャマセット | 1 | 1.52% | 1 |
| すきがきパジャマセット | 1 | 1.52% | 1 |
| すきがきパジャマセット | 1 | 1.52% | 1 |
| すきがきパジャマセット | 1 | 1.52% | 1 |
| minecraft:emerald_block | 30 | 45.45% | 23-48 |
| 達人の釣り竿 | 1 | 1.52% | 1 |
| すきがき釣りセット | 1 | 1.52% | 1 |
| すきがき釣りセット | 1 | 1.52% | 1 |
| すきがき釣りセット | 1 | 1.52% | 1 |
| すきがき釣りセット | 1 | 1.52% | 1 |
| ?ハニカム-∞-777? | 1 | 1.52% | 1 |
| !ハニカム-∅-000! | 1 | 1.52% | 1 |
| 伝説の角笛 | 1 | 1.52% | 1 |
| 隕石でできた薪割り斧 | 1 | 1.52% | 1 |
| 成金ダイバーメット | 1 | 1.52% | 1 |
| 10連ガチャボックス（BEYOND） | 1 | 1.52% | 1 |
| artifacts:fire_gauntlet | 1 | 1.52% | 1 |
| artifacts:snorkel | 1 | 1.52% | 1 |
| artifacts:aqua_dashers | 1 | 1.52% | 1 |
| artifacts:helium_flamingo | 1 | 1.52% | 1 |
| artifacts:mimic_spawn_egg | 1 | 1.52% | 1 |
| 彼方の釣り竿 | 1 | 1.52% | 1 |
| 彼方のすきがきトニック | 1 | 1.52% | 1 |
| すきがきの澱み残留瓶 | 1 | 1.52% | 1 |
| 彼方のすきがきしぶき | 1 | 1.52% | 1 |
| 彼方のすきがき釣り竿 | 1 | 1.52% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.52% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.52% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.52% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.52% | 1 |
| SKGKYRの頭 | 1 | 1.52% | 1 |

## CELESTIAL（セレスティアル）ティア

**合計ウェイト: 68**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| |||双称の祝福牌||| | 1 | 1.47% | 1 |
| |||闘気の祝福牌||| | 1 | 1.47% | 1 |
| |||重撃の祝福牌||| | 1 | 1.47% | 1 |
| |||生命の祝福牌||| | 1 | 1.47% | 1 |
| |||幸福の祝福牌||| | 1 | 1.47% | 1 |
| |||鋼鉄の祝福牌||| | 1 | 1.47% | 1 |
| |||迅速の祝福牌||| | 1 | 1.47% | 1 |
| |||破滅の呪印牌||| | 1 | 1.47% | 1 |
| |||弱気の呪印牌||| | 1 | 1.47% | 1 |
| |||軽撃の呪印牌||| | 1 | 1.47% | 1 |
| |||死の呪印牌||| | 1 | 1.47% | 1 |
| |||不運の呪印牌||| | 1 | 1.47% | 1 |
| |||脆弱の呪印牌||| | 1 | 1.47% | 1 |
| |||鈍重の呪印牌||| | 1 | 1.47% | 1 |
| ~コンジット-CS-ショート~ | 4 | 5.88% | 1 |
| ~コンジット-CM-ミディアム~ | 3 | 4.41% | 1 |
| ~コンジット-CL-ロング~ | 2 | 2.94% | 1 |
| ~コンジット✰CS✰ショートII~ | 3 | 4.41% | 1 |
| ~コンジット✰CM✰ミディアムII~ | 2 | 2.94% | 1 |
| ~コンジット✰CL✰ロングII~ | 1 | 1.47% | 1 |
| minecraft:emerald_block | 15 | 22.06% | 30-60 |
| 極上の釣り竿 | 1 | 1.47% | 1 |
| すきがき釣りセット | 1 | 1.47% | 1 |
| すきがき釣りセット | 1 | 1.47% | 1 |
| すきがき釣りセット | 1 | 1.47% | 1 |
| すきがき釣りセット | 1 | 1.47% | 1 |
| minecraft:end_portal_frame | 1 | 1.47% | 1-2 |
| 星空のコンパウンドボウ | 2 | 2.94% | 1 |
| 発光するヤバい水 | 2 | 2.94% | 1 |
| 10連ガチャボックス（CELESTIAL） | 1 | 1.47% | 1 |
| artifacts:universal_attractor | 1 | 1.47% | 1 |
| artifacts:night_vision_goggles | 1 | 1.47% | 1 |
| artifacts:obsidian_skull | 1 | 1.47% | 1 |
| artifacts:chorus_totem | 1 | 1.47% | 1 |
| 星海の釣り竿 | 1 | 1.47% | 1 |
| 星海のすきがきネクター | 1 | 1.47% | 1 |
| 星海のすきがき星霧 | 1 | 1.47% | 1 |
| 星海のすきがき釣り竿 | 1 | 1.47% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.47% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.47% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.47% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.47% | 1 |
| SKGKYRの頭 | 1 | 1.47% | 1 |

## DIVINE（ディバイン）ティア

**合計ウェイト: 58**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| すきがきパジャマセット | 1 | 1.72% | 1 |
| すきがきパジャマセット | 1 | 1.72% | 1 |
| すきがきパジャマセット | 1 | 1.72% | 1 |
| すきがきパジャマセット | 1 | 1.72% | 1 |
| minecraft:emerald_block | 20 | 34.48% | 90-180 |
| ◊◊至高の釣り竿◊◊ | 4 | 6.90% | 1 |
| すきがき釣りセット | 1 | 1.72% | 1 |
| すきがき釣りセット | 1 | 1.72% | 1 |
| すきがき釣りセット | 1 | 1.72% | 1 |
| すきがき釣りセット | 1 | 1.72% | 1 |
| minecraft:end_portal_frame | 2 | 3.45% | 1-3 |
| 伝説のトライデント | 1 | 1.72% | 1 |
| 伝説の盾 | 1 | 1.72% | 1 |
| 伝説の秘薬 | 1 | 1.72% | 1 |
| 10連ガチャボックス（DIVINE） | 1 | 1.72% | 1 |
| artifacts:vampiric_glove | 1 | 1.72% | 1 |
| artifacts:lucky_scarf | 1 | 1.72% | 1 |
| artifacts:cross_necklace | 1 | 1.72% | 1 |
| 神託の釣り竿 | 1 | 1.72% | 1 |
| cataclysm:witherite_ingot | 2 | 3.45% | 2-4 |
| cataclysm:ignitium_ingot | 2 | 3.45% | 1-2 |
| cataclysm:lionfish | 2 | 3.45% | 1-3 |
| cataclysm:azure_sea_shield | 1 | 1.72% | 1 |
| cataclysm:tidal_claws | 1 | 1.72% | 1 |
| 神託のすきがき聖水 | 1 | 1.72% | 1 |
| 神託のすきがき瀑布 | 1 | 1.72% | 1 |
| 神託のすきがき釣り竿 | 1 | 1.72% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.72% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.72% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.72% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.72% | 1 |
| SKGKYRの頭 | 1 | 1.72% | 1 |

## COSMIC（コズミック）ティア

**合計ウェイト: 89**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| ◆◇◆永遠の双称の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の闘気の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の重撃の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の生命の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の幸福の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の鋼鉄の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永遠の迅速の祝福牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の破滅の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の弱気の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の軽撃の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の死の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の不運の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の脆弱の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ◆◇◆永劫の鈍重の呪印牌◆◇◆ | 1 | 1.12% | 1 |
| ~コンジット✰∞✰アトランティス~ | 1 | 1.12% | 1 |
| すきがき戦闘服セット | 1 | 1.12% | 1 |
| すきがき戦闘服セット | 1 | 1.12% | 1 |
| すきがき戦闘服セット | 1 | 1.12% | 1 |
| すきがき戦闘服セット | 1 | 1.12% | 1 |
| すきがきパジャマセット | 1 | 1.12% | 1 |
| すきがきパジャマセット | 1 | 1.12% | 1 |
| すきがきパジャマセット | 1 | 1.12% | 1 |
| すきがきパジャマセット | 1 | 1.12% | 1 |
| minecraft:emerald_block | 25 | 28.09% | 180-334 |
| ◈◈究極の釣り竿◈◈ | 1 | 1.12% | 1 |
| すきがき安眠セット | 1 | 1.12% | 1 |
| すきがき安眠セット | 1 | 1.12% | 1 |
| すきがき安眠セット | 1 | 1.12% | 1 |
| すきがき安眠セット | 1 | 1.12% | 1 |
| minecraft:end_portal_frame | 3 | 3.37% | 2-5 |
| minecraft:ender_dragon_spawn_egg | 1 | 1.12% | 1 |
| minecraft:wither_spawn_egg | 1 | 1.12% | 1 |
| すきがきの裏大剣 | 1 | 1.12% | 1 |
| 宇宙素材のダイビングスーツ | 1 | 1.12% | 1 |
| すきがきの最終兵器 | 1 | 1.12% | 1 |
| 10連ガチャボックス（COSMIC） | 1 | 1.12% | 1 |
| artifacts:eternal_steak | 1 | 1.12% | 1 |
| artifacts:cloud_in_a_bottle | 1 | 1.12% | 1 |
| artifacts:crystal_heart | 1 | 1.12% | 1 |
| 流星の釣り竿 | 1 | 1.12% | 1 |
| cataclysm:enderite_ingot | 3 | 3.37% | 1-3 |
| cataclysm:void_crystal | 3 | 3.37% | 2-4 |
| cataclysm:void_core | 2 | 2.25% | 1 |
| cataclysm:void_forge | 1 | 1.12% | 1 |
| cataclysm:void_assault_shoulder_weapon | 1 | 1.12% | 1 |
| cataclysm:void_jaw | 1 | 1.12% | 1 |
| 宇宙すきがきエリクサー | 1 | 1.12% | 1 |
| 宇宙すきがきノヴァ | 1 | 1.12% | 1 |
| 宇宙すきがき釣り竿 | 1 | 1.12% | 1 |
| すきがき釣りセット | 1 | 1.12% | 1 |
| すきがき釣りセット | 1 | 1.12% | 1 |
| すきがき釣りセット | 1 | 1.12% | 1 |
| すきがき釣りセット | 1 | 1.12% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.12% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.12% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.12% | 1 |
| 呪われたすきがき釣りセット | 1 | 1.12% | 1 |
| SKGKYRの頭 | 1 | 1.12% | 1 |

## GODティア

**合計ウェイト: 30**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| チェキ券 | 1 | 3.33% | 1 |
| 10連ガチャボックス（GOD） | 1 | 3.33% | 1 |
| 神の釣り竿 | 1 | 3.33% | 1 |
| cataclysm:the_incinerator | 1 | 3.33% | 1 |
| cataclysm:infernal_forge | 1 | 3.33% | 1 |
| cataclysm:gauntlet_of_guard | 1 | 3.33% | 1 |
| cataclysm:bulwark_of_the_flame | 1 | 3.33% | 1 |
| cataclysm:wither_assault_shoulder_weapon | 1 | 3.33% | 1 |
| cataclysm:meat_shredder | 1 | 3.33% | 1 |
| cataclysm:laser_gatling | 1 | 3.33% | 1 |
| cataclysm:soul_render | 1 | 3.33% | 1 |
| cataclysm:ceraunus | 1 | 3.33% | 1 |
| cataclysm:ancient_spear | 1 | 3.33% | 1 |
| cataclysm:flame_eye | 1 | 3.33% | 1 |
| cataclysm:abyss_eye | 1 | 3.33% | 1 |
| cataclysm:storm_eye | 1 | 3.33% | 1 |
| cataclysm:ignitium_upgrade_smithing_template | 2 | 6.67% | 1 |
| すきがき神の祝杯 | 1 | 3.33% | 1 |
| すきがき神の恵みの霧 | 1 | 3.33% | 1 |
| すきがき神の御竿 | 1 | 3.33% | 1 |
| すきがき釣りセット | 1 | 3.33% | 1 |
| すきがき釣りセット | 1 | 3.33% | 1 |
| すきがき釣りセット | 1 | 3.33% | 1 |
| すきがき釣りセット | 1 | 3.33% | 1 |
| 呪われたすきがき釣りセット | 1 | 3.33% | 1 |
| 呪われたすきがき釣りセット | 1 | 3.33% | 1 |
| 呪われたすきがき釣りセット | 1 | 3.33% | 1 |
| 呪われたすきがき釣りセット | 1 | 3.33% | 1 |
| SKGKYRの頭 | 1 | 3.33% | 1 |

## UNIQUEティア

**合計ウェイト: 14**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| TEST | 1 | 7.14% | 1 |
| minecraft:emerald_block | 8 | 57.14% | 14-33 |
| 10連ガチャボックス（UNIQUE） | 1 | 7.14% | 1 |
| ポセイドンの釣り竿 | 1 | 7.14% | 1 |
| 深淵より来たりし幼子 | 1 | 7.14% | 1 |
| 小さき怪物のバケツ | 1 | 7.14% | 1 |
| 大渦の篭手 | 1 | 7.14% | 1 |

## DOLPHINS_GRACE（イルカの好意）ティア

**合計ウェイト: 96**

| アイテム | ウェイト | 確率 | 数量 |
|---|---|---|---|
| 謎の頭 | 70 | 72.92% | 1 |
| minecraft:nautilus_shell | 20 | 20.83% | 1 |
| minecraft:trident | 5 | 5.21% | 1 |
| 10連ガチャボックス（DOLPHINS_GRACE） | 1 | 1.04% | 1 |

## 総合幸運計算

### 基本幸運値

| 要素 | 線形範囲の計算式 | 線形範囲 | 線形範囲でのボーナス | 超過分（対数カーブ） |
|---|---|---|---|---|
| **宝釣りエンチャント** | レベル × 0.50% | 1～10レベル | +0.50% ～ +5.00% | +2.5 × ln(1 + 超過Lv)（Lv127まで） |
| **幸運ポーション** | レベル × 1.00% | 1～5レベル | +1.00% ～ +5.00% | +1.0 × ln(1 + 超過Lv) |
| **不幸ポーション** | レベル × -2.00% | 1～10レベル | -2.00% ～ -20.00% | -2.0 × ln(1 + 超過Lv) |
| **装備幸運** | 値 × 0.15% | スロットごと -50～+100 | -7.50% ～ +15.00%/スロット | ±5.0 × ln(1 + 超過分) ポイント換算 |
| **経験値レベル** | レベル × 0.02% | 0～100レベル | 0.00% ～ +2.00% | +0.25 × ln(1 + 超過Lv) |

- 宝釣りの例: Lv10 = +5.0% / Lv20 = +11.0% / Lv50 = +14.3% / Lv127 = +16.9%（Lv127が効果の上限）
- マイナス宝釣り（呪い竿）はプラス側と対称のカーブでペナルティになる。
- 装備幸運は6スロット（頭・胴・脚・足・メインハンド・オフハンド）を個別にソフトクランプした後、合計値もスロット範囲×6でソフトクランプされる。
- **Jobs連携**: 対象職業（デフォルト: Fisherman）に就いているプレイヤーは、釣り抽選時に職業レベル10ごとに幸運ポーションLv+1相当として扱われる（例: 職業Lv30 → 幸運Lv3 = +3.0%、Lv50 → 幸運Lv5 = +5.0%、Lv100 → 幸運Lv10 ≈ +6.8%）。エフェクトは付与されず、実際の幸運ポーションと重複した場合は加算せず高い方を採用。複数の対象職業に就いている場合は最高職業レベルを使用（`jobs_luck` 設定）。

### 総合幸運値の計算

**総合幸運値** = **宝釣り** + **幸運ポーション** + **不幸ポーション** + **装備幸運** + **経験値** + **天気ボーナス** + **タイミングボーナス**

- 幸運ポーションと不幸ポーションは自動的に相殺される（例: 幸運Lv3 + 不幸Lv2 = 幸運Lv1相当）。
- 総合幸運値は **-25.0% ～ +25.0%** にクランプされる（`luck_calculation.min_total_luck` / `max_total_luck`）。
- この総合幸運値がカテゴリ条件の `min_total_luck` 判定と確率補正に使用される。

### 特殊幸運ボーナス（宝釣りLv127以上）

宝釣りLv127以上かつコンジットパワー効果を所持している場合、コンジットレベルに応じた固定ボーナスが加算される：

**特殊ボーナス** = min(10, コンジットレベル) × 0.5%

| コンジットレベル | ボーナス |
|---|---|
| Lv1 | +0.5% |
| Lv2 | +1.0% |
| Lv3 | +1.5% |
| … | … |
| Lv10以上 | +5.0%（上限） |

### 天気ボーナス

| 天気 | ボーナス |
|---|---|
| **晴れ** | +0.0% |
| **雨** | +0.35% |
| **雷雨** | +0.70% |

※ 浮きの32ブロック上空内に`雨雲 (Rainy Cloud)` が存在すれば*雨*扱いになります。

### タイミングボーナス

ベースボーナス1.8% × ティア倍率で計算される。

| 判定 | 反応時間 | 倍率 | ボーナス |
|---|---|---|---|
| **JUST** | 50ms以内 | 50% | +0.9% |
| **PERFECT** | 100ms以内 | 40% | +0.72% |
| **GREAT** | 300ms以内 | 30% | +0.54% |
| **GOOD** | 750ms以内 | 20% | +0.36% |
| **MISS** | 750ms超過 | 0% | +0.0% |

### 属性タイプ(装備幸運値)

Minecraftの属性修飾子には3つの操作タイプが存在し、それぞれ異なる計算方法を使用します：

| 属性 | 説明 | 計算式 |
|---|---|---|
| **add_value** | 修飾子の値を基本値に加算 | `合計 = 基本値 + 値1 + 値2 + ... + 値n` |
| **add_multiplied_base** | 基本値に(1 + 修飾子の合計)を乗算 | `合計 = 基本値 × (1 + 値1 + 値2 + ... + 値n)` |
| **add_multiplied_total** | 前段階の合計に(1 + 値)を乗算 | `合計 = 前段階合計 × (1 + 値1) × (1 + 値2) × ... × (1 + 値n)` |

### 最大・最小幸運値

各要素の合計がいくら大きくても、総合幸運値は **±25.0%** でクランプされる。

上限到達の例（雷雨時）:

- 宝釣りLv127: **+16.9%**
- 特殊ボーナス（コンジットLv10以上）: **+5.0%**
- 幸運ポーションLv10: **+6.8%**（Lv5=+5.0% + 1.0×ln(6)）
- 雷雨: **+0.7%**
- JUSTタイミング: **+0.9%**
- 合計 +30.3% → クランプで **+25.0%**

下限は不幸ポーションLv10（-20%）＋マイナス装備幸運などで **-25.0%** に到達しうる。

### ダブルフィッシング

- **発動条件**: 宝釣りLv10以上かつコンジットパワーLv2以上
- **効果**:
  - 釣りが2回分同時に処理される
  - タイミング判定は1回だけ行い、両方の結果に適用される

## 確率計算式

### カテゴリ抽選の補正

#### 正の幸運時

品質（Quality）が0より大きいカテゴリのみ乗算補正が適用される：

```
scaledLuck = ln(1 + 総幸運値 × luckScale)
qualityFactor = ln(1 + quality × qualityImpact)
multiplier = min(1.0 + scaledLuck × qualityFactor, maxMultiplier)
adjustedChance = baseChance × multiplier × rampFactor
```

Quality 0 のカテゴリ（god / cosmic / divine / unique / dolphins_grace）は multiplier がかからず、しきい値ランプのみが適用される。

#### しきい値ランプ

`min_total_luck` が1以上のカテゴリは、解禁した瞬間に満額の重みで抽選されるのではなく、しきい値をどれだけ超えているかに応じて重みが徐々に増える（段差解禁の防止）：

```
rampFactor = min_factor + (1 - min_factor) × min(1, (総幸運値 - min_total_luck) / range)
```

例（range 8, min_factor 0.05）: god（min_total_luck 14）は総幸運14で重み5%、18で約53%、22以上で100%。
`min_total_luck` が0のカテゴリは常に rampFactor = 1.0。

#### 負の幸運時

```
penalty = |総幸運値| × penaltyScale × quality
adjustedChance = max(baseChance - penalty, 0.0)
```

### 設定値

| パラメータ | デフォルト値 | 説明 |
|---|---|---|
| maxMultiplier | 3.0 | 最大倍率（3倍まで） |
| luckScale | 0.1 | 運の影響度 |
| qualityImpact | 0.5 | 品質の影響度 |
| penaltyScale | 0.05 | 負の幸運ペナルティスケール |
| threshold_ramp.range | 8.0 | 満額の重みに達するまでに必要な超過幸運値 |
| threshold_ramp.min_factor | 0.05 | 解禁直後の重み係数 |

## 入れ食い（Lure）高レベル竿の挙動

バニラは入れ食いLv6以上になると浮きが一切沈まなくなるため、上限（`lure_behavior_cap: 5`）を超える竿はプラグインが待ち時間を独自計算する：

- Lv5相当（1～100tick）を基準に、超過1レベルごとに最大待ち時間を1tick短縮
- Lv105以上でほぼ着水即ヒット（最短1tick）

## 束縛の呪い機能

### 概要

束縛の呪いエンチャントが付いたアイテムは、釣り上げた本人のみが使用・所持できます。

### 機能詳細

- **所有者設定**: 釣り上げた瞬間にプレイヤーのUUIDと名前を保存
- **制限範囲**: アイテム拾得、インベントリ操作、使用、ドロップ、手持ち変更など全操作
- **表示**: アイテムのLoreに「束縛: [プレイヤー名] のみ所持・使用可能」を表示
- **メッセージ**: 他プレイヤーが操作を試みると警告メッセージを表示
