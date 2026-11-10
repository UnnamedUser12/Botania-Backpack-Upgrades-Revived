package velrondevs.botaniabackpack.datagen.providers.recipes;

import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.p3pp3rf1y.sophisticatedcore.crafting.HoldingRecipeOutput;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;

import java.util.concurrent.CompletableFuture;

public final class UpgradeRecipeProvider extends RecipeProvider {
	private UpgradeRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
		super(registries, output);
	}

	@Override
	protected void buildRecipes() {
		shaped(RecipeCategory.MISC, ModItems.MANA_TANK_UPGRADE.get())
				.define('B', item("botania", "mana_pool"))
				.define('L', item("botania", "livingrock"))
				.define('M', tag("botania", "manasteel_ingots"))
				.define('P', item("botania", "mana_pearl"))
				.define('T', item("sophisticatedbackpacks", "tank_upgrade"))
				.pattern("MPM")
				.pattern("LTL")
				.pattern("MBM")
				.unlockedBy("has_tank_upgrade", has(itemLike("sophisticatedbackpacks", "tank_upgrade")))
				.save(output);

		shaped(RecipeCategory.MISC, ModItems.MANA_SUPPLY_UPGRADE.get())
				.define('M', tag("botania", "manasteel_ingots"))
				.define('P', item("botania", "mana_pearl"))
				.define('R', item("sophisticatedbackpacks", "refill_upgrade"))
				.define('S', item("botania", "spark"))
				.define('T', item("botania", "mana_tablet"))
				.pattern("MSM")
				.pattern("PRP")
				.pattern("MTM")
				.unlockedBy("has_refill_upgrade", has(itemLike("sophisticatedbackpacks", "refill_upgrade")))
				.save(output);

		shaped(RecipeCategory.MISC, ModItems.MANA_INFUSION_UPGRADE.get())
				.define('D', item("botania", "mana_powder"))
				.define('M', tag("botania", "manasteel_ingots"))
				.define('P', item("botania", "mana_pearl"))
				.define('R', item("botania", "rune_mana"))
				.define('S', item("sophisticatedbackpacks", "smelting_upgrade"))
				.pattern("MDM")
				.pattern("PSP")
				.pattern("MRM")
				.unlockedBy("has_smelting_upgrade", has(itemLike("sophisticatedbackpacks", "smelting_upgrade")))
				.save(output);

		saveNextTier(shaped(RecipeCategory.MISC, ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get())
				.define('D', tag("botania", "dragonstone_gems"))
				.define('E', tag("botania", "elementium_ingots"))
				.define('P', item("botania", "pixie_dust"))
				.define('U', ModItems.MANA_INFUSION_UPGRADE.get())
				.pattern(" E ")
				.pattern("DUD")
				.pattern(" P ")
				.unlockedBy("has_mana_infusion_upgrade", has(ModItems.MANA_INFUSION_UPGRADE.get())));
	}

	private void saveNextTier(ShapedRecipeBuilder builder) {
		HoldingRecipeOutput holder = new HoldingRecipeOutput(output.advancement());
		builder.save(holder);
		ShapedRecipe composed = (ShapedRecipe) holder.getRecipe();
		ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, ModItems.ADVANCED_MANA_INFUSION_UPGRADE.getId());
		output.accept(key, new UpgradeNextTierRecipe(composed), holder.getAdvancementHolder());
	}

	private static Ingredient item(String namespace, String path) {
		return Ingredient.of(itemLike(namespace, path));
	}

	private static ItemLike itemLike(String namespace, String path) {
		Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
		Item item = BuiltInRegistries.ITEM.getValue(id);
		if (item == Items.AIR) {
			throw new IllegalStateException("Missing item for recipe datagen: " + id);
		}
		return item;
	}

	private Ingredient tag(String namespace, String path) {
		return tag(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(namespace, path)));
	}

	public static final class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
			super(output, registries);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
			return new UpgradeRecipeProvider(registries, output);
		}

		@Override
		public String getName() {
			return "Botania Backpack Upgrades recipes";
		}
	}
}
