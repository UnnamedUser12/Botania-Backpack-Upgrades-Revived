# Changelog

## 1.0.1
- Fixed a crash on startup with Sophisticated Core 1.5.x / Sophisticated Backpacks 3.26.x (`NoClassDefFoundError: UpgradeGuiManager$IUpgradeInventoryPartFactory`).
- Updated the mana tank bar to the new Sophisticated Core GUI API (`UpgradeInventoryControlBase` / `UpgradeGuiManager.registerInventoryControl`).
- Minimum versions raised to Sophisticated Core 1.5.19 and Sophisticated Backpacks 3.26.17 (the 1.4.x / 3.25.x line is no longer supported). Tested with Core 1.5.19 and 1.5.23, Backpacks 3.26.17 and 3.26.19.
- Added tests that statically check every reference of the mod to Sophisticated Core, Sophisticated Backpacks and Botania against the installed jars, including the client-only GUI code.

## 1.0.0
- Port of Botania Backpack Upgrades (original mod by suiyi) to Minecraft 1.21.1 / NeoForge 21.1.x.
- Built against Botania Revived 1.0.3 (`velrondevs.botania`) and Sophisticated Backpacks 3.25 / Sophisticated Core 1.4 for 1.21.1.
- Mana Tank, Mana Supply, Mana Infusion and Advanced Mana Infusion upgrades.
- Upgrade state (mana, transfer mode) now stored in data components instead of raw NBT.
- Botania capabilities (mana receiver, spark attachable, mana item, wand HUD) migrated to the NeoForge capability system.
- Own creative tab "Botania Backpack Upgrades" with the four upgrades (they are still added to the Sophisticated Backpacks tab).
- Source reorganized under the root package `velrondevs.botaniabackpack`: `registry/` (one class per registry type), `datagen/` (lang, recipes, item models and tags are generated; run `data`), `upgrade/`, `compat/botania/`, `client/`, `event/`, `config/`.
- 48 GameTests (mana tank, mana transfer in all three modes, mana supply, mana infusion, recipes, persistence, Botania mana network) in a separate `gametest` source set that is not part of the release jar. The upstream GameTests and JUnit tests are not included.
