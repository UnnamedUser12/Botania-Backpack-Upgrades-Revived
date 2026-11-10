package velrondevs.botaniabackpack.datagen.providers.models;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public final class UpgradeItemModelProvider extends ModelProvider {
	public UpgradeItemModelProvider(PackOutput output) {
		super(output, BotaniaBackpackUpgrades.MOD_ID);
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		itemModels.generateFlatItem(ModItems.MANA_TANK_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(ModItems.MANA_SUPPLY_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(ModItems.MANA_INFUSION_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
		itemModels.generateFlatItem(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get(), ModelTemplates.FLAT_ITEM);
	}
}
