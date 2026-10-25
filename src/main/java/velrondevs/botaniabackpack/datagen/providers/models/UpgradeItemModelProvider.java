package velrondevs.botaniabackpack.datagen.providers.models;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class UpgradeItemModelProvider extends ItemModelProvider {
	public UpgradeItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, BotaniaBackpackUpgrades.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		basicItem(ModItems.MANA_TANK_UPGRADE.get());
		basicItem(ModItems.MANA_SUPPLY_UPGRADE.get());
		basicItem(ModItems.MANA_INFUSION_UPGRADE.get());
		basicItem(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get());
	}
}
