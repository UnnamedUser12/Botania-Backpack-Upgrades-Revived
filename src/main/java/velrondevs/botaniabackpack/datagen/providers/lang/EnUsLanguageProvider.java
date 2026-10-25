package velrondevs.botaniabackpack.datagen.providers.lang;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModCreativeTabs;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class EnUsLanguageProvider extends LanguageProvider {
	public EnUsLanguageProvider(PackOutput output) {
		super(output, BotaniaBackpackUpgrades.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		addItem(ModItems.MANA_TANK_UPGRADE, "Mana Tank Upgrade");
		add("item.botaniabackpack.mana_tank_upgrade.tooltip",
				"Stores mana while the backpack is placed.\nActs as a Botania mana pool for bursts, spreaders, sparks, and mana consumers.");
		add("gui.botaniabackpack.status.add.mana_tank_exists", "Only one Mana Tank Upgrade can be installed in a backpack.");
		add("gui.botaniabackpack.upgrade.mana_tank", "Mana Tank");
		add("gui.botaniabackpack.upgrade.mana_tank.tooltip", "Configure mana transfer direction");
		add("gui.botaniabackpack.transfer_mode.both", "Input and output");
		add("gui.botaniabackpack.transfer_mode.input", "Input only");
		add("gui.botaniabackpack.transfer_mode.output", "Output only");
		add("gui.botaniabackpack.mana_tank.contents", "%s / %s mana");
		add("gui.botaniabackpack.mana_tank.interaction", "Right-click with a mana item to transfer; direction follows the transfer mode");

		addItem(ModItems.MANA_SUPPLY_UPGRADE, "Mana Supply Upgrade");
		add("item.botaniabackpack.mana_supply_upgrade.tooltip",
				"Allows compatible mana-consuming equipment carried by the player to draw from the Mana Tank in the same backpack on demand.\nDoes not automatically charge items stored inside the backpack.");
		add("gui.botaniabackpack.status.add.mana_supply_exists", "Only one Mana Supply Upgrade can be installed in a backpack.");

		addItem(ModItems.MANA_INFUSION_UPGRADE, "Mana Infusion Upgrade");
		add("item.botaniabackpack.mana_infusion_upgrade.tooltip",
				"Uses the Mana Tank in the same backpack to transform filtered items with mana-infusion recipes.\nOnly recipes without a catalyst are supported.");
		addItem(ModItems.ADVANCED_MANA_INFUSION_UPGRADE, "Advanced Mana Infusion Upgrade");
		add("item.botaniabackpack.advanced_mana_infusion_upgrade.tooltip",
				"Uses the Mana Tank in the same backpack for mana infusion.\nAn Alchemy or Conjuration Catalyst below a placed backpack enables its recipes.\nHas more filtering options.");
		add("gui.botaniabackpack.status.add.mana_infusion_exists", "Only one Mana Infusion Upgrade can be installed in a backpack.");
		add("gui.botaniabackpack.upgrade.mana_infusion", "Mana Infusion");
		add("gui.botaniabackpack.upgrade.mana_infusion.tooltip", "Mana infusion settings");
		add("gui.botaniabackpack.upgrade.advanced_mana_infusion", "Advanced Mana Infusion");
		add("gui.botaniabackpack.upgrade.advanced_mana_infusion.tooltip", "Advanced mana infusion settings");

		add("fluid_type.botaniabackpack.mana_display", "Mana");
		add(ModCreativeTabs.MAIN_TAB_TRANSLATION_KEY, "Botania Backpack Upgrades");
	}
}
