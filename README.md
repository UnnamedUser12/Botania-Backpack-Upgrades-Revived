# Botania Backpack Upgrades

一个适用于 Minecraft 1.20.1 Forge 的兼容模组，为精妙背包添加魔力储罐、魔力供给和两级魔力灌注升级。放置后的背包可通过植物魔法正式能力接口表现为魔力池；被玩家携带时，魔力储罐也可按需为兼容装备供魔。

## 已实现

- 魔力保存在升级物品自身的 NBT 中；背包被挖走、搬运或重新放置时不会丢失。
- 默认容量为 1,000,000 mana，可在世界的服务端配置中调整。
- 接收魔力发射器发出的魔力脉冲。
- 相邻魔力发射器可从背包抽取魔力；按植物魔法原版逻辑，背包应放在发射器背面。
- 支持普通火花及扩散、聚集、离散等火花升级的标准传输逻辑。
- 注册为植物魔法魔力网络中的魔力池，可供使用公开 `ManaPool` API 的功能花、魔力分配器和魔力镜等设备使用。
- 升级页可在“双向 / 仅输入 / 仅输出”之间切换。
- 在魔力储罐页用单个魔力物品右击储罐槽，可按当前传输模式在物品与储罐之间一次性充放魔力；该交互属于储罐基础功能，不要求魔力供给升级。
- 安装魔力供给升级后，主手、副手、物品栏、胸甲位及精妙背包已登记的饰品/背包槽中的背包，可作为植物魔法标准 `ManaItem` 魔力源，让兼容武器、工具、盔甲和饰品按需耗用储罐魔力。
- 魔力供给不进行每刻装备扫描，也不会主动给物品充能；只在装备实际发起 Botania 标准魔力请求时工作。
- 魔力供给和魔力灌注属于背包内部用能，不受储罐的外部传输模式限制；右键物品充放则严格遵循“双向 / 仅输入 / 仅输出”。
- 同一个背包即使被主物品栏、饰品兼容层等多个来源同时发现，也只会计入一次，避免精确请求被重复统计。
- 可与精妙背包的流体储罐升级同时安装；背包界面会像电池与流体储罐共存时一样分栏显示，放置后的背包模型则使用左右两个独立储罐位置。
- 森林法杖指向背包时显示准确的魔力量 HUD。
- 普通魔力灌注升级会读取完整的 `botania:mana_infusion` 配方集合，支持原版、数据包及其他模组追加的不带催化剂配方。
- 高级魔力灌注升级在背包方块化时检测正下方的方块状态，支持炼金催化器、凝聚催化器以及数据包配方声明的兼容催化剂。
- 灌注升级使用同一背包内魔力储罐的魔力，不给魔力装备或其他物品充能。
- 默认处理漏斗、管道等自动输入；“在 GUI 中工作”按钮可进一步允许处理玩家在背包界面中放入或改动的物品。
- 普通版提供黑/白名单；高级版还可按物品、模组或物品标签匹配，并且不显示 NBT、耐久匹配按钮。
- 转化前先模拟输出能否完整放入背包，空间不足时不扣输入和魔力；凝聚及循环配方的产物不会被误当成新输入反复加工。
- 中英文语言文件。

## 明确不支持

- 向背包方块上方投掷物品不会像原版魔力池一样触发转化；魔力灌注升级只处理背包库存。
- 向方块世界中投掷魔力物品不会充放能；支持的是背包界面中用光标所持魔力物品右击魔力储罐槽。
- 在植物魔法中硬编码要求 `ManaPoolBlock` 或 `ManaPoolBlockEntity` 的多方块结构，例如精灵门。这些逻辑没有公开兼容接口，模组不会用 Mixin 伪装方块类型。

## 构建

需要 Java 17：

```powershell
.\gradlew.bat build
```

开发依赖已固定到 Botania `1.20.1-453-FORGE`、Sophisticated Backpacks `3.24.60.1982` 和 Sophisticated Core `1.3.71.2181`。

运行完整的服务端集成测试：

```powershell
.\gradlew.bat runGameTestServer
```

该测试会使用真实注册表和数据标签放置背包、安装升级、读写 Botania 能力、切换传输方向，并验证随身按需供魔、魔力物品双向充放、精确请求原子性、普通灌注、炼金催化、凝聚复制防递归及魔力池网络生命周期。纯逻辑边界测试由常规 `build` 中的 JUnit 测试执行。

## 配置

首次进入世界后可在服务端配置文件中修改：

```toml
[manaTank]
capacity = 1000000

[manaInfusion]
filterSlots = 9
slotsInRow = 3

[advancedManaInfusion]
filterSlots = 16
slotsInRow = 4
```

配置使用 Forge 的 `SERVER` 类型，联机时由服务端同步给客户端，避免容量显示与实际逻辑不一致。

详细的游戏内验证步骤见 [docs/manual-test-plan.md](docs/manual-test-plan.md)。

---

## NeoForge 1.21.1 port notes

This directory is a NeoForge port (by UnnamedUser12) of the original mod (https://github.com/taisuiyileba/Botania-Backpack-Upgrades, MIT, by suiyi, based on upstream commit fdc34c1) to Minecraft 1.21.1 / NeoForge 21.1.240, built against Botania Revived 1.0.3 (package `velrondevs.botania`), Sophisticated Core 1.5.19 and Sophisticated Backpacks 3.26.17 (minimum versions) for 1.21.1. The text above is the original (Chinese) README of the 1.20.1 Forge version. The upstream GameTests and JUnit tests are not part of this port.
