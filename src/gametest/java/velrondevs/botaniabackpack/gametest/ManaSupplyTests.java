package velrondevs.botaniabackpack.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManaSupplyTests {
	private static final String TEMPLATE = "empty";

	private static ItemStack backpackWithTankAndSupply(int mana) {
		ItemStack backpack = TestSupport.backpackWithTank(mana);
		TestSupport.install(TestSupport.wrapper(backpack), ModItems.MANA_SUPPLY_UPGRADE.get());
		return backpack;
	}

	private static ManaTankUpgradeWrapper tank(ItemStack backpack) {
		return TestSupport.tank(TestSupport.wrapper(backpack));
	}

	@GameTest(template = TEMPLATE)
	public static void supplyLetsInventoryBackpackPowerPlayerItems(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = backpackWithTankAndSupply(10_000);
		player.getInventory().setItem(5, backpack);
		ItemStack consumer = new ItemStack(Items.DIAMOND_SWORD);

		helper.assertTrue(ManaItemHandler.instance().getManaItems(player).stream().anyMatch(s -> s == backpack),
				"backpack with tank+supply is listed as a mana item of the player");
		int got = ManaItemHandler.instance().requestMana(consumer, player, 1_234, true);
		helper.assertTrue(got == 1_234, "got " + got);
		helper.assertTrue(tank(backpack).getMana() == 10_000 - 1_234, "tank now " + tank(backpack).getMana());

		helper.assertTrue(ManaItemHandler.instance().requestManaExact(consumer, player, 1_000, true), "exact request satisfied");
		helper.assertTrue(tank(backpack).getMana() == 10_000 - 2_234, "tank after exact request " + tank(backpack).getMana());

		// Simulation (remove=false) does not consume
		helper.assertTrue(ManaItemHandler.instance().requestMana(consumer, player, 500, false) == 500, "simulated request reports 500");
		helper.assertTrue(tank(backpack).getMana() == 10_000 - 2_234, "simulation did not consume");

		// More than available: partial for requestMana, refusal and no change for requestManaExact
		int rest = tank(backpack).getMana();
		helper.assertTrue(!ManaItemHandler.instance().requestManaExact(consumer, player, rest + 1, true), "exact request beyond stock fails");
		helper.assertTrue(tank(backpack).getMana() == rest, "failed exact request leaves mana untouched");
		helper.assertTrue(ManaItemHandler.instance().requestMana(consumer, player, rest + 5_000, true) == rest, "partial request returns what exists");
		helper.assertTrue(tank(backpack).getMana() == 0, "tank drained");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void supplyWorksForBackpackWornInTheChestSlot(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = backpackWithTankAndSupply(5_000);
		player.setItemSlot(EquipmentSlot.CHEST, backpack);
		ItemStack consumer = new ItemStack(Items.IRON_PICKAXE);

		helper.assertTrue(ManaItemHandler.instance().requestMana(consumer, player, 800, true) == 800, "worn backpack supplies mana");
		helper.assertTrue(tank(player.getItemBySlot(EquipmentSlot.CHEST)).getMana() == 4_200, "worn tank drained by exactly 800");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void backpackCountsOnlyOnceWhenFoundBySeveralSources(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = backpackWithTankAndSupply(10_000);
		player.getInventory().setItem(0, backpack);
		// Same backpack data also present as offhand copy: same storage UUID must not double count
		ItemStack duplicateView = backpack.copy();
		player.getInventory().offhand.set(0, duplicateView);

		long listed = ManaItemHandler.instance().getManaItems(player).stream()
				.filter(s -> s.getItem() == backpack.getItem()).count();
		helper.assertTrue(listed == 1, "same storage must be listed once but was listed " + listed + " times");
		helper.assertTrue(!ManaItemHandler.instance().requestManaExact(new ItemStack(Items.STICK), player, 12_000, true),
				"cannot request more than the single tank holds");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void withoutSupplyUpgradeThePlayerItemsGetNothing(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = TestSupport.backpackWithTank(10_000); // tank only
		player.getInventory().setItem(2, backpack);
		player.setItemSlot(EquipmentSlot.CHEST, TestSupport.backpackWithTank(10_000));
		ItemStack consumer = new ItemStack(Items.DIAMOND_SWORD);

		helper.assertTrue(ManaItemHandler.instance().requestMana(consumer, player, 100, true) == 0, "no supply -> no mana");
		helper.assertTrue(tank(backpack).getMana() == 10_000, "tank untouched");
		helper.assertTrue(TestSupport.manaItem(backpack) == null, "no ManaItem capability without the supply upgrade");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void supplyWithoutTankProvidesNothing(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = TestSupport.newBackpack();
		TestSupport.install(TestSupport.wrapper(backpack), ModItems.MANA_SUPPLY_UPGRADE.get());
		player.getInventory().setItem(2, backpack);
		helper.assertTrue(TestSupport.manaItem(backpack) == null, "supply without tank exposes no mana item");
		helper.assertTrue(ManaItemHandler.instance().requestMana(new ItemStack(Items.DIAMOND_SWORD), player, 100, true) == 0, "nothing to give");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void supplyOnlyExportsAndDoesNotAcceptManaFromOthers(GameTestHelper helper) {
		ItemStack backpack = backpackWithTankAndSupply(1_000);
		var manaItem = TestSupport.manaItem(backpack);
		helper.assertTrue(manaItem != null, "has capability");
		helper.assertTrue(manaItem.getMana() == 1_000 && manaItem.getMaxMana() == 1_000_000, "values reflect the tank");
		manaItem.addMana(500); // positive additions are ignored: the backpack is a source only
		helper.assertTrue(tank(backpack).getMana() == 1_000, "positive addMana ignored");
		manaItem.addMana(-300);
		helper.assertTrue(tank(backpack).getMana() == 700, "negative addMana consumes");
		helper.assertTrue(!manaItem.canReceiveManaFromItem(new ItemStack(Items.STICK)) && !manaItem.canReceiveManaFromPool(null), "does not receive");
		helper.assertTrue(manaItem.canExportManaToItem(new ItemStack(Items.STICK)) && !manaItem.canExportManaToPool(null), "exports to items only");
		helper.succeed();
	}

	@SuppressWarnings("unused")
	private static void unused(Player p) {}
}
