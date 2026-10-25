package velrondevs.botaniabackpack.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import velrondevs.botania.api.mana.ManaItem;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.compat.botania.ManaItemTransferHelper;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManaTransferTests {
	private static final String TEMPLATE = "empty";

	private static ItemStack tablet(int mana) {
		ItemStack tablet = new ItemStack(TestSupport.botaniaItem("mana_tablet"));
		ManaItem item = TestSupport.manaItem(tablet);
		if (item == null) {
			throw new IllegalStateException("mana tablet has no ManaItem capability");
		}
		if (mana > 0) {
			item.addMana(mana);
		}
		return tablet;
	}

	private static int tabletMana(ItemStack tablet) {
		return TestSupport.manaItem(tablet).getMana();
	}

	@GameTest(template = TEMPLATE)
	public static void inputModeMovesManaFromItemIntoTank(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(100);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		ItemStack tablet = tablet(5_000);

		int moved = TestSupport.interact(tank, tablet, ManaTransferMode.INPUT);
		helper.assertTrue(moved == 5_000, "moved " + moved);
		helper.assertTrue(tank.getMana() == 5_100, "tank has " + tank.getMana());
		helper.assertTrue(tabletMana(tablet) == 0, "tablet has " + tabletMana(tablet));

		// Nothing left to take
		helper.assertTrue(TestSupport.interact(tank, tablet, ManaTransferMode.INPUT) == 0, "empty tablet moves nothing");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void outputModeMovesManaFromTankIntoItem(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(8_000);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		ItemStack tablet = tablet(0);

		int moved = TestSupport.interact(tank, tablet, ManaTransferMode.OUTPUT);
		helper.assertTrue(moved == 8_000, "moved " + moved);
		helper.assertTrue(tank.getMana() == 0, "tank has " + tank.getMana());
		helper.assertTrue(tabletMana(tablet) == 8_000, "tablet has " + tabletMana(tablet));
		helper.assertTrue(TestSupport.interact(tank, tablet, ManaTransferMode.OUTPUT) == 0, "empty tank moves nothing");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void bothModePrefersFillingTheItemThenDrainsItWhenTankIsEmpty(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(3_000);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		ItemStack tablet = tablet(0);

		helper.assertTrue(TestSupport.interact(tank, tablet, ManaTransferMode.BOTH) == 3_000, "tank -> tablet first");
		helper.assertTrue(tank.getMana() == 0 && tabletMana(tablet) == 3_000, "tank empty, tablet full of the 3000");
		helper.assertTrue(TestSupport.interact(tank, tablet, ManaTransferMode.BOTH) == 3_000, "now tablet -> tank");
		helper.assertTrue(tank.getMana() == 3_000 && tabletMana(tablet) == 0, "mana returned, nothing lost");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void transfersConserveManaAtCapacityLimits(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(0);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		int capacity = tank.getCapacity();
		ItemStack source = tablet(0);
		ManaItem sourceItem = TestSupport.manaItem(source);
		int tabletCapacity = sourceItem.getMaxMana();
		helper.assertTrue(tabletCapacity > 0, "tablet capacity positive");

		// Fill the tank almost completely, then push more from a tablet than fits
		tank.receiveInternalMana(capacity - 1_000);
		sourceItem.addMana(Math.min(tabletCapacity, 5_000));
		int before = tank.getMana() + tabletMana(source);
		int moved = TestSupport.interact(tank, source, ManaTransferMode.INPUT);
		helper.assertTrue(moved == 1_000, "only the free space is moved, moved " + moved);
		helper.assertTrue(tank.getMana() == capacity, "tank is exactly full");
		helper.assertTrue(tank.getMana() + tabletMana(source) == before, "mana is conserved on INPUT");

		// Now drain the tank into a tablet that has less room than the tank holds
		ItemStack almostFull = tablet(0);
		ManaItem almostFullItem = TestSupport.manaItem(almostFull);
		int room = 2_500;
		almostFullItem.addMana(almostFullItem.getMaxMana() - room);
		int before2 = tank.getMana() + tabletMana(almostFull);
		int moved2 = TestSupport.interact(tank, almostFull, ManaTransferMode.OUTPUT);
		helper.assertTrue(moved2 == room, "only the room in the item is moved, moved " + moved2);
		helper.assertTrue(tabletMana(almostFull) == almostFullItem.getMaxMana(), "tablet is exactly full");
		helper.assertTrue(tank.getMana() + tabletMana(almostFull) == before2, "mana is conserved on OUTPUT");
		helper.assertTrue(TestSupport.interact(tank, almostFull, ManaTransferMode.OUTPUT) == 0, "full item accepts nothing");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void cursorClickOnTankBarTransfersManaThroughTheMenuPacket(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = TestSupport.backpackWithTank(0);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		tank.receiveInternalMana(2_000);
		ManaTankUpgradeContainer container = ManaTankUpgradeContainer.TYPE.create(player, 3, tank);

		// Right click on the bar with an empty tablet in the cursor, mode BOTH: tank -> tablet
		player.containerMenu.setCarried(tablet(0));
		CompoundTag click = new CompoundTag();
		click.putBoolean("cursorInteraction", true);
		container.handlePacket(click);
		helper.assertTrue(tank.getMana() == 0, "tank drained, has " + tank.getMana());
		helper.assertTrue(tabletMana(player.containerMenu.getCarried()) == 2_000, "carried tablet got the mana");

		// Same click again with a tank in INPUT-only mode and a loaded tablet
		tank.setTransferMode(ManaTransferMode.INPUT);
		player.containerMenu.setCarried(tablet(700));
		container.handlePacket(click);
		helper.assertTrue(tank.getMana() == 700, "INPUT click fills the tank, has " + tank.getMana());
		helper.assertTrue(tabletMana(player.containerMenu.getCarried()) == 0, "tablet emptied");

		// OUTPUT mode with a loaded tablet must not touch anything
		tank.setTransferMode(ManaTransferMode.OUTPUT);
		player.containerMenu.setCarried(tablet(400));
		container.handlePacket(click);
		helper.assertTrue(tabletMana(player.containerMenu.getCarried()) == 1_100 && tank.getMana() == 0,
				"OUTPUT click moves tank (700) into the 400 tablet: tablet=" + tabletMana(player.containerMenu.getCarried()) + " tank=" + tank.getMana());

		// A non mana item or a stack of several items is ignored
		player.containerMenu.setCarried(new ItemStack(Items.STICK));
		container.handlePacket(click);
		ItemStack two = tablet(100);
		two.setCount(2);
		player.containerMenu.setCarried(two);
		tank.receiveInternalMana(50);
		tank.setTransferMode(ManaTransferMode.BOTH);
		container.handlePacket(click);
		helper.assertTrue(tank.getMana() == 50, "stack of 2 must be ignored");

		// Transfer-mode message from the GUI button
		CompoundTag mode = new CompoundTag();
		mode.putString("transferMode", "input");
		container.handlePacket(mode);
		helper.assertTrue(tank.getTransferMode() == ManaTransferMode.INPUT, "mode packet applied");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void helperNeverTransfersWhenBackpackIsTheItself(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(100);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		helper.assertTrue(ManaItemTransferHelper.interact(tank, backpack, backpack, ManaTransferMode.BOTH) == 0, "same stack ignored");
		helper.assertTrue(ManaItemTransferHelper.interact(tank, backpack, ItemStack.EMPTY, ManaTransferMode.BOTH) == 0, "empty ignored");
		helper.assertTrue(ManaItemTransferHelper.interact(tank, backpack, new ItemStack(Items.DIRT), ManaTransferMode.BOTH) == 0, "non-mana item ignored");
		helper.assertTrue(tank.getMana() == 100, "tank untouched");
		helper.succeed();
	}
}
