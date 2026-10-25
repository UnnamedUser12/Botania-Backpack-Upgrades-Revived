# Changelog

## 1.0.0
- Port of Botania Backpack Upgrades (original mod by suiyi) to Minecraft 1.21.1 / NeoForge 21.1.x.
- Built against Botania Revived 1.0.3 (`velrondevs.botania`) and Sophisticated Backpacks 3.25 / Sophisticated Core 1.4 for 1.21.1.
- Mana Tank, Mana Supply, Mana Infusion and Advanced Mana Infusion upgrades.
- Upgrade state (mana, transfer mode) now stored in data components instead of raw NBT.
- Botania capabilities (mana receiver, spark attachable, mana item, wand HUD) migrated to the NeoForge capability system.
- Own creative tab "Botania Backpack Upgrades" with the four upgrades (they are still added to the Sophisticated Backpacks tab).
- Source reorganized under the root package `velrondevs.botaniabackpack`: `registry/` (one class per registry type), `datagen/` (lang, recipes, item models and tags are generated; run `data`), `upgrade/`, `compat/botania/`, `client/`, `event/`, `config/`.
- 48 GameTests (mana tank, mana transfer in all three modes, mana supply, mana infusion, recipes, persistence, Botania mana network) in a separate `gametest` source set that is not part of the release jar. The upstream GameTests and JUnit tests are not included.
