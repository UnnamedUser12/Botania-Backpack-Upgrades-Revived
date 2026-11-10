package velrondevs.botaniabackpack.datagen.providers.tags;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public final class UpgradeItemTagProvider extends ItemTagsProvider {
	private static final TagKey<Item> BACKPACK_UPGRADES = TagKey.create(Registries.ITEM,
			Identifier.fromNamespaceAndPath("sophisticatedbackpacks", "upgrade"));

	public UpgradeItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		super(output, lookup, BotaniaBackpackUpgrades.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BACKPACK_UPGRADES)
				.add(ModItems.MANA_TANK_UPGRADE.get())
				.add(ModItems.MANA_SUPPLY_UPGRADE.get())
				.add(ModItems.MANA_INFUSION_UPGRADE.get())
				.add(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get());
	}
}
