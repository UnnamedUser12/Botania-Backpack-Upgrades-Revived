package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModDataComponents;
import velrondevs.botaniabackpack.registry.ModItems;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;

import java.util.List;
import java.util.Optional;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeTests {
	private static final String TEMPLATE = "empty";

	private static Item sb(String path) {
		return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", path));
	}

	private static Item bot(String path) {
		return TestSupport.botaniaItem(path);
	}

	private static ItemStack craft(GameTestHelper helper, Item... grid) {
		return craftStacks(helper, java.util.Arrays.stream(grid).map(item -> item == null ? ItemStack.EMPTY : new ItemStack(item)).toList());
	}

	private static ItemStack craftStacks(GameTestHelper helper, List<ItemStack> grid) {
		CraftingInput input = CraftingInput.of(3, 3, grid);
		Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
		helper.assertTrue(recipe.isPresent(), "no crafting recipe matched the grid");
		return recipe.get().value().assemble(input, helper.getLevel().registryAccess());
	}

	@GameTest(template = TEMPLATE)
	public static void manaTankUpgradeRecipe(GameTestHelper helper) {
		ItemStack result = craft(helper,
				bot("manasteel_ingot"), bot("mana_pearl"), bot("manasteel_ingot"),
				bot("livingrock"), sb("tank_upgrade"), bot("livingrock"),
				bot("manasteel_ingot"), bot("mana_pool"), bot("manasteel_ingot"));
		helper.assertTrue(result.is(ModItems.MANA_TANK_UPGRADE.get()), "expected mana tank upgrade, got " + result);
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void manaSupplyUpgradeRecipe(GameTestHelper helper) {
		ItemStack result = craft(helper,
				bot("manasteel_ingot"), bot("spark"), bot("manasteel_ingot"),
				bot("mana_pearl"), sb("refill_upgrade"), bot("mana_pearl"),
				bot("manasteel_ingot"), bot("mana_tablet"), bot("manasteel_ingot"));
		helper.assertTrue(result.is(ModItems.MANA_SUPPLY_UPGRADE.get()), "expected mana supply upgrade, got " + result);
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void manaInfusionUpgradeRecipe(GameTestHelper helper) {
		ItemStack result = craft(helper,
				bot("manasteel_ingot"), bot("mana_powder"), bot("manasteel_ingot"),
				bot("mana_pearl"), sb("smelting_upgrade"), bot("mana_pearl"),
				bot("manasteel_ingot"), bot("rune_mana"), bot("manasteel_ingot"));
		helper.assertTrue(result.is(ModItems.MANA_INFUSION_UPGRADE.get()), "expected mana infusion upgrade, got " + result);
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void advancedInfusionUpgradeRecipeKeepsTheComponentsOfTheBasicUpgrade(GameTestHelper helper) {
		ItemStack basic = new ItemStack(ModItems.MANA_INFUSION_UPGRADE.get());
		basic.set(ModDataComponents.MANA.get(), 4_321);
		basic.set(ModDataComponents.TRANSFER_MODE.get(), ManaTransferMode.INPUT);
		ItemStack result = craftStacks(helper, List.of(
				ItemStack.EMPTY, new ItemStack(bot("elementium_ingot")), ItemStack.EMPTY,
				new ItemStack(bot("dragonstone")), basic, new ItemStack(bot("dragonstone")),
				ItemStack.EMPTY, new ItemStack(bot("pixie_dust")), ItemStack.EMPTY));
		helper.assertTrue(result.is(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get()), "expected advanced infusion upgrade, got " + result);
		helper.assertTrue(result.getOrDefault(ModDataComponents.MANA.get(), 0) == 4_321, "upgrade_next_tier must carry the component data over");
		helper.assertTrue(result.get(ModDataComponents.TRANSFER_MODE.get()) == ManaTransferMode.INPUT, "transfer mode carried over");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void upgradesAreInTheSophisticatedUpgradeTag(GameTestHelper helper) {
		var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,
				ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "upgrade"));
		for (var upgrade : List.of(ModItems.MANA_TANK_UPGRADE, ModItems.MANA_SUPPLY_UPGRADE, ModItems.MANA_INFUSION_UPGRADE,
				ModItems.ADVANCED_MANA_INFUSION_UPGRADE)) {
			helper.assertTrue(new ItemStack(upgrade.get()).is(tag), upgrade.getId() + " must be tagged sophisticatedbackpacks:upgrade");
		}
		helper.succeed();
	}
}
