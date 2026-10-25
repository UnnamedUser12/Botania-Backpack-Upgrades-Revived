package velrondevs.botaniabackpack.datagen.providers.tags;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** Registers the upgrades in the Sophisticated Backpacks item tag {@code sophisticatedbackpacks:upgrade}. */
public final class UpgradeItemTagProvider extends TagsProvider<Item> {
	private static final TagKey<Item> BACKPACK_UPGRADES = TagKey.create(Registries.ITEM,
			ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "upgrade"));

	public UpgradeItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFileHelper) {
		super(output, Registries.ITEM, lookup, BotaniaBackpackUpgrades.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BACKPACK_UPGRADES)
				.add(ModItems.MANA_TANK_UPGRADE.getKey())
				.add(ModItems.MANA_SUPPLY_UPGRADE.getKey())
				.add(ModItems.MANA_INFUSION_UPGRADE.getKey())
				.add(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.getKey());
	}
}
