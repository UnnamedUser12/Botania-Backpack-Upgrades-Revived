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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.p3pp3rf1y.sophisticatedcore.crafting.HoldingRecipeOutput;
import net.p3pp3rf1y.sophisticatedcore.crafting.UpgradeNextTierRecipe;

import java.util.concurrent.CompletableFuture;

public final class UpgradeRecipeProvider extends RecipeProvider {
	public UpgradeRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MANA_TANK_UPGRADE.get())
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

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MANA_SUPPLY_UPGRADE.get())
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

		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MANA_INFUSION_UPGRADE.get())
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

		saveNextTier(output, ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get())
				.define('D', tag("botania", "dragonstone_gems"))
				.define('E', tag("botania", "elementium_ingots"))
				.define('P', item("botania", "pixie_dust"))
				.define('U', ModItems.MANA_INFUSION_UPGRADE.get())
				.pattern(" E ")
				.pattern("DUD")
				.pattern(" P ")
				.unlockedBy("has_mana_infusion_upgrade", has(ModItems.MANA_INFUSION_UPGRADE.get())));
	}

	/**
	 * Saves the recipe wrapped in Sophisticated Core's {@code upgrade_next_tier} recipe, which carries the
	 * previous upgrade's data components over to the result. Unlike Core's own builder, no
	 * {@code item_enabled} condition is added.
	 */
	private static void saveNextTier(RecipeOutput output, ShapedRecipeBuilder builder) {
		HoldingRecipeOutput holder = new HoldingRecipeOutput(output.advancement());
		builder.save(holder);
		ShapedRecipe composed = (ShapedRecipe) holder.getRecipe();
		output.accept(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.getId(), new UpgradeNextTierRecipe(composed), holder.getAdvancementHolder());
	}

	/** Items of other mods are looked up by registry name; they are loaded during the datagen run. */
	private static Ingredient item(String namespace, String path) {
		return Ingredient.of(itemLike(namespace, path));
	}

	private static ItemLike itemLike(String namespace, String path) {
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
		Item item = BuiltInRegistries.ITEM.get(id);
		if (item == Items.AIR) {
			throw new IllegalStateException("Missing item for recipe datagen: " + id);
		}
		return item;
	}

	private static Ingredient tag(String namespace, String path) {
		return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path)));
	}
}
