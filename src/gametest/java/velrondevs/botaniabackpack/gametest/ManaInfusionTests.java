package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManaInfusionTests {
	private static final String TEMPLATE = "empty";
	private static final BlockPos BACKPACK = new BlockPos(1, 2, 1);
	private static final BlockPos BELOW = new BlockPos(1, 1, 1);

	/** iron_ingot -> manasteel_ingot costs 3000 mana and needs no catalyst (botania:mana_infusion/manasteel). */
	private static final int MANASTEEL_COST = 3_000;
	/** cobblestone -> sand costs 50 mana and needs the alchemy catalyst. */
	private static final int SAND_COST = 50;
	/** coal -> 2x coal costs 2100 mana and needs the conjuration catalyst. */
	private static final int COAL_COST = 2_100;

	private static int count(IStorageWrapper wrapper, Item item) {
		InventoryHandler inventory = wrapper.getInventoryHandler();
		int total = 0;
		for (int slot = 0; slot < inventory.getSlots(); slot++) {
			ItemStack stack = inventory.getStackInSlot(slot);
			if (stack.is(item)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	private static ItemStack backpack(int mana, boolean advanced, boolean withTank, Item... allowed) {
		ItemStack backpack = withTank ? TestSupport.backpackWithTank(mana) : TestSupport.newBackpack();
		TestSupport.install(TestSupport.wrapper(backpack), advanced
				? ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get() : ModItems.MANA_INFUSION_UPGRADE.get());
		// The infusion filter is an allow list by default (empty = nothing is processed), so the tests whitelist their inputs
		var filterHandler = TestSupport.infusion(TestSupport.wrapper(backpack)).getFilterLogic().getFilterHandler();
		for (int i = 0; i < allowed.length; i++) {
			filterHandler.setStackInSlot(i, new ItemStack(allowed[i]));
		}
		return backpack;
	}

	private static void insertAutomatically(IStorageWrapper wrapper, ItemStack stack) {
		ItemStack remainder = wrapper.getInventoryForInputOutput().insertItem(stack, false);
		if (!remainder.isEmpty()) {
			throw new IllegalStateException("Could not insert " + stack);
		}
	}

	@GameTest(template = TEMPLATE)
	public static void basicUpgradeInfusesInsertedItemsAndSpendsTheRightMana(GameTestHelper helper) {
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack(MANASTEEL_COST * 5, false, true, Items.IRON_INGOT));
		IStorageWrapper wrapper = be.getBackpackWrapper();
		insertAutomatically(wrapper, new ItemStack(Items.IRON_INGOT, 3));

		helper.runAfterDelay(10, () -> {
			Item manasteel = TestSupport.botaniaItem("manasteel_ingot");
			helper.assertTrue(count(wrapper, manasteel) == 3, "3 manasteel ingots expected, found " + count(wrapper, manasteel));
			helper.assertTrue(count(wrapper, Items.IRON_INGOT) == 0, "iron consumed, left " + count(wrapper, Items.IRON_INGOT));
			helper.assertTrue(TestSupport.tank(wrapper).getMana() == MANASTEEL_COST * 2,
					"mana left should be " + MANASTEEL_COST * 2 + " but was " + TestSupport.tank(wrapper).getMana());
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void infusionStopsWhenManaRunsOutWithoutLosingItems(GameTestHelper helper) {
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack(MANASTEEL_COST + 2_999, false, true, Items.IRON_INGOT));
		IStorageWrapper wrapper = be.getBackpackWrapper();
		insertAutomatically(wrapper, new ItemStack(Items.IRON_INGOT, 4));

		helper.runAfterDelay(10, () -> {
			Item manasteel = TestSupport.botaniaItem("manasteel_ingot");
			helper.assertTrue(count(wrapper, manasteel) == 1, "only one conversion affordable, got " + count(wrapper, manasteel));
			helper.assertTrue(count(wrapper, Items.IRON_INGOT) == 3, "3 iron must remain, found " + count(wrapper, Items.IRON_INGOT));
			helper.assertTrue(TestSupport.tank(wrapper).getMana() == 2_999, "mana left " + TestSupport.tank(wrapper).getMana());

			// Topping the tank up later resumes the pending work without re-inserting
			TestSupport.tank(wrapper).receiveInternalMana(MANASTEEL_COST * 3);
			helper.runAfterDelay(10, () -> {
				helper.assertTrue(count(wrapper, manasteel) == 4, "all converted after refill, got " + count(wrapper, manasteel));
				helper.succeed();
			});
		});
	}

	@GameTest(template = TEMPLATE)
	public static void infusionDoesNothingWithoutManaOrWithoutTank(GameTestHelper helper) {
		BackpackBlockEntity empty = TestSupport.placeBackpack(helper, BACKPACK, backpack(0, false, true, Items.IRON_INGOT));
		BackpackBlockEntity noTank = TestSupport.placeBackpack(helper, new BlockPos(2, 2, 1), backpack(0, false, false, Items.IRON_INGOT));
		insertAutomatically(empty.getBackpackWrapper(), new ItemStack(Items.IRON_INGOT, 2));
		insertAutomatically(noTank.getBackpackWrapper(), new ItemStack(Items.IRON_INGOT, 2));

		helper.runAfterDelay(10, () -> {
			Item manasteel = TestSupport.botaniaItem("manasteel_ingot");
			helper.assertTrue(count(empty.getBackpackWrapper(), manasteel) == 0 && count(empty.getBackpackWrapper(), Items.IRON_INGOT) == 2,
					"tank without mana must not convert");
			helper.assertTrue(count(noTank.getBackpackWrapper(), manasteel) == 0 && count(noTank.getBackpackWrapper(), Items.IRON_INGOT) == 2,
					"no tank must not convert");
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void infusionRespectsTheFilter(GameTestHelper helper) {
		ItemStack backpack = backpack(MANASTEEL_COST * 10, false, true);
		var filter = TestSupport.infusion(TestSupport.wrapper(backpack)).getFilterLogic();
		filter.setAllowList(true);
		filter.getFilterHandler().setStackInSlot(0, new ItemStack(Items.GOLD_INGOT)); // only gold is allowed; iron is not
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack);
		insertAutomatically(be.getBackpackWrapper(), new ItemStack(Items.IRON_INGOT, 2));

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(count(be.getBackpackWrapper(), Items.IRON_INGOT) == 2, "iron not in the allow list stays untouched");
			helper.assertTrue(TestSupport.tank(be.getBackpackWrapper()).getMana() == MANASTEEL_COST * 10, "no mana spent");
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void infusionKeepsInputsAndManaWhileTheOutputDoesNotFitThenResumes(GameTestHelper helper) {
		ItemStack backpack = backpack(MANASTEEL_COST * 3, false, true, Items.IRON_INGOT);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack);
		IStorageWrapper wrapper = be.getBackpackWrapper();
		InventoryHandler inventory = wrapper.getInventoryHandler();
		Item manasteel = TestSupport.botaniaItem("manasteel_ingot");
		// Every slot is full of cobblestone except slot 0, which holds the iron: converting would leave no room for the output
		for (int slot = 1; slot < inventory.getSlots(); slot++) {
			inventory.setStackInSlot(slot, new ItemStack(Items.COBBLESTONE, 64));
		}
		insertAutomaticallyIntoSlot(wrapper, new ItemStack(Items.IRON_INGOT, 2));

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(count(wrapper, manasteel) == 0, "no room for the output: nothing may convert, found " + count(wrapper, manasteel));
			helper.assertTrue(count(wrapper, Items.IRON_INGOT) == 2, "no iron may be lost, found " + count(wrapper, Items.IRON_INGOT));
			helper.assertTrue(TestSupport.tank(wrapper).getMana() == MANASTEEL_COST * 3, "no mana may be spent, left " + TestSupport.tank(wrapper).getMana());

			inventory.setStackInSlot(1, ItemStack.EMPTY); // free one slot
			helper.runAfterDelay(10, () -> {
				helper.assertTrue(count(wrapper, manasteel) == 2, "conversion resumes once there is room, found " + count(wrapper, manasteel));
				helper.assertTrue(count(wrapper, Items.IRON_INGOT) == 0, "iron consumed");
				helper.assertTrue(TestSupport.tank(wrapper).getMana() == MANASTEEL_COST, "paid for exactly two conversions, left " + TestSupport.tank(wrapper).getMana());
				helper.succeed();
			});
		});
	}

	private static void insertAutomaticallyIntoSlot(IStorageWrapper wrapper, ItemStack stack) {
		ItemStack remainder = wrapper.getInventoryForInputOutput().insertItem(stack, false);
		if (!remainder.isEmpty()) {
			throw new IllegalStateException("Could not insert " + stack);
		}
	}

	@GameTest(template = TEMPLATE)
	public static void advancedUpgradeUsesTheAlchemyCatalystBelowThePlacedBackpack(GameTestHelper helper) {
		Block catalyst = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", "alchemy_catalyst"));
		helper.setBlock(BELOW, catalyst);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack(1_000, true, true, Items.COBBLESTONE));
		IStorageWrapper wrapper = be.getBackpackWrapper();
		insertAutomatically(wrapper, new ItemStack(Items.COBBLESTONE, 10));

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(count(wrapper, Items.SAND) == 10, "10 sand expected, found " + count(wrapper, Items.SAND));
			helper.assertTrue(count(wrapper, Items.COBBLESTONE) == 0, "cobblestone consumed");
			helper.assertTrue(TestSupport.tank(wrapper).getMana() == 1_000 - 10 * SAND_COST,
					"mana left " + TestSupport.tank(wrapper).getMana());
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void advancedUpgradeUsesTheConjurationCatalystAndMultipliesTheOutput(GameTestHelper helper) {
		Block catalyst = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", "conjuration_catalyst"));
		helper.setBlock(BELOW, catalyst);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, BACKPACK, backpack(COAL_COST * 2, true, true, Items.COAL));
		IStorageWrapper wrapper = be.getBackpackWrapper();
		insertAutomatically(wrapper, new ItemStack(Items.COAL, 2));

		helper.runAfterDelay(15, () -> {
			// The cloned coal must not be reprocessed as new input: 2 coal in -> exactly 4 coal out, exactly 2 payments
			helper.assertTrue(count(wrapper, Items.COAL) == 4, "2 coal -> 4 coal expected, found " + count(wrapper, Items.COAL));
			helper.assertTrue(TestSupport.tank(wrapper).getMana() == 0, "mana left " + TestSupport.tank(wrapper).getMana());
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void catalystRecipesNeedTheAdvancedUpgradeAndTheCatalyst(GameTestHelper helper) {
		Block catalyst = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", "alchemy_catalyst"));
		helper.setBlock(BELOW, catalyst);
		// Basic upgrade on top of the catalyst: no catalyst recipes
		BackpackBlockEntity basic = TestSupport.placeBackpack(helper, BACKPACK, backpack(1_000, false, true, Items.COBBLESTONE));
		insertAutomatically(basic.getBackpackWrapper(), new ItemStack(Items.COBBLESTONE, 4));
		// Advanced upgrade without a catalyst below
		BackpackBlockEntity noCatalyst = TestSupport.placeBackpack(helper, new BlockPos(2, 2, 1), backpack(1_000, true, true, Items.COBBLESTONE));
		insertAutomatically(noCatalyst.getBackpackWrapper(), new ItemStack(Items.COBBLESTONE, 4));

		helper.runAfterDelay(10, () -> {
			helper.assertTrue(count(basic.getBackpackWrapper(), Items.SAND) == 0, "basic must not use catalyst recipes");
			helper.assertTrue(count(noCatalyst.getBackpackWrapper(), Items.SAND) == 0, "advanced without catalyst converts nothing");
			helper.assertTrue(TestSupport.tank(basic.getBackpackWrapper()).getMana() == 1_000, "no mana spent (basic)");
			helper.assertTrue(TestSupport.tank(noCatalyst.getBackpackWrapper()).getMana() == 1_000, "no mana spent (no catalyst)");
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void carriedBackpackInfusesOnlyWhileWorkInGuiIsEnabled(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = backpack(MANASTEEL_COST * 2, false, true, Items.IRON_INGOT);
		IStorageWrapper wrapper = TestSupport.wrapper(backpack);
		player.getInventory().setItem(3, backpack);
		var infusion = TestSupport.infusion(wrapper);
		wrapper.getInventoryHandler().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 2));

		infusion.tick(player, helper.getLevel(), player.blockPosition());
		helper.assertTrue(count(wrapper, TestSupport.botaniaItem("manasteel_ingot")) == 0, "GUI work disabled: manual stacking is not processed");

		infusion.setShouldWorkInGUI(true);
		helper.assertTrue(infusion.shouldWorkInGUI(), "flag persisted in data component");
		infusion.queueAllMatchingSlots();
		infusion.tick(player, helper.getLevel(), player.blockPosition());
		infusion.tick(player, helper.getLevel(), player.blockPosition());
		helper.assertTrue(count(wrapper, TestSupport.botaniaItem("manasteel_ingot")) == 2,
				"GUI work enabled converts, got " + count(wrapper, TestSupport.botaniaItem("manasteel_ingot")));
		helper.assertTrue(TestSupport.tank(wrapper).getMana() == 0, "mana spent " + TestSupport.tank(wrapper).getMana());
		helper.succeed();
	}
}
