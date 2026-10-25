package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import velrondevs.botania.api.BotaniaAPI;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.ManaPool;
import velrondevs.botania.api.mana.ManaReceiver;
import velrondevs.botania.api.mana.spark.SparkAttachable;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.compat.botania.ManaBackpackPool;
import velrondevs.botaniabackpack.registry.ModDataComponents;
import velrondevs.botaniabackpack.registry.ModItems;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;

import java.util.Set;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManaTankTests {
	private static final String TEMPLATE = "empty";

	@GameTest(template = TEMPLATE)
	public static void tankStoresManaAndClampsToCapacity(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(0);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		int capacity = tank.getCapacity();
		helper.assertTrue(capacity == 1_000_000, "default capacity should be 1,000,000 but was " + capacity);
		helper.assertTrue(tank.getMana() == 0, "new tank must be empty");

		helper.assertTrue(tank.receiveInternalMana(400) == 400, "should accept 400");
		helper.assertTrue(tank.getMana() == 400, "mana should be 400 but was " + tank.getMana());
		helper.assertTrue(tank.receiveInternalMana(Integer.MAX_VALUE) == capacity - 400, "should only accept the free space");
		helper.assertTrue(tank.getMana() == capacity, "tank should be full");
		helper.assertTrue(tank.receiveInternalMana(1) == 0, "full tank accepts nothing");
		helper.assertTrue(tank.getInternalAvailableSpace() == 0, "no space left");

		helper.assertTrue(tank.extractInternalMana(Integer.MAX_VALUE) == capacity, "should extract everything");
		helper.assertTrue(tank.getMana() == 0, "tank should be empty");
		helper.assertTrue(tank.extractInternalMana(5) == 0, "empty tank gives nothing");
		helper.assertTrue(!tank.tryConsumeMana(1), "cannot consume from empty tank");
		tank.receiveInternalMana(10);
		helper.assertTrue(!tank.tryConsumeMana(11) && tank.getMana() == 10, "failed consume must not change mana");
		helper.assertTrue(tank.tryConsumeMana(10) && tank.getMana() == 0, "exact consume works");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void tankStateSurvivesSerialization(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(12_345);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));
		tank.setTransferMode(ManaTransferMode.OUTPUT);

		// The upgrade stack itself: persistent codec and network codec of both data components
		ItemStack upgradeStack = tank.getUpgradeStack();
		helper.assertTrue(upgradeStack.get(ModDataComponents.MANA.get()) == 12_345, "mana component set");
		helper.assertTrue(upgradeStack.get(ModDataComponents.TRANSFER_MODE.get()) == ManaTransferMode.OUTPUT, "mode component set");

		Tag saved = upgradeStack.save(helper.getLevel().registryAccess());
		ItemStack loaded = ItemStack.parse(helper.getLevel().registryAccess(), saved).orElseThrow();
		helper.assertTrue(loaded.get(ModDataComponents.MANA.get()) == 12_345, "mana persisted through NBT");
		helper.assertTrue(loaded.get(ModDataComponents.TRANSFER_MODE.get()) == ManaTransferMode.OUTPUT, "mode persisted through NBT");

		RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), helper.getLevel().registryAccess());
		ItemStack.STREAM_CODEC.encode(buf, upgradeStack);
		ItemStack decoded = ItemStack.STREAM_CODEC.decode(buf);
		helper.assertTrue(decoded.get(ModDataComponents.MANA.get()) == 12_345, "mana synced through network codec");
		helper.assertTrue(decoded.get(ModDataComponents.TRANSFER_MODE.get()) == ManaTransferMode.OUTPUT, "mode synced through network codec");

		// The whole backpack: stack round-trip keeps pointing at the same storage
		Tag savedBackpack = backpack.save(helper.getLevel().registryAccess());
		ItemStack reloadedBackpack = ItemStack.parse(helper.getLevel().registryAccess(), savedBackpack).orElseThrow();
		ManaTankUpgradeWrapper reloadedTank = TestSupport.tank(BackpackWrapper.fromStack(reloadedBackpack));
		helper.assertTrue(reloadedTank.getMana() == 12_345, "mana after backpack reload = " + reloadedTank.getMana());
		helper.assertTrue(reloadedTank.getTransferMode() == ManaTransferMode.OUTPUT, "mode after backpack reload");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void transferModeControlsExternalNetworkIo(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(100);
		ManaTankUpgradeWrapper tank = TestSupport.tank(TestSupport.wrapper(backpack));

		tank.setTransferMode(ManaTransferMode.INPUT);
		tank.receiveMana(50);
		helper.assertTrue(tank.getMana() == 150, "INPUT accepts");
		tank.receiveMana(-50);
		helper.assertTrue(tank.getMana() == 150, "INPUT must not give mana to the network");
		helper.assertTrue(tank.getManaAvailableForOutput() == 0, "INPUT exposes nothing");

		tank.setTransferMode(ManaTransferMode.OUTPUT);
		tank.receiveMana(50);
		helper.assertTrue(tank.getMana() == 150, "OUTPUT must refuse mana from the network");
		tank.receiveMana(-50);
		helper.assertTrue(tank.getMana() == 100, "OUTPUT releases mana");
		helper.assertTrue(tank.getManaAvailableForOutput() == 100, "OUTPUT exposes stored mana");
		helper.assertTrue(tank.isFullForInput(), "OUTPUT only tank is 'full' for input");

		tank.setTransferMode(ManaTransferMode.BOTH);
		helper.assertTrue(tank.getAvailableSpace() == tank.getCapacity() - 100, "BOTH reports free space");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void placedBackpackExposesBotaniaCapabilitiesOnlyWithTank(GameTestHelper helper) {
		BlockPos plain = new BlockPos(1, 1, 1);
		BlockPos withTank = new BlockPos(2, 1, 1);
		TestSupport.placeBackpack(helper, plain, TestSupport.newBackpack());
		TestSupport.placeBackpack(helper, withTank, TestSupport.backpackWithTank(0));

		var level = helper.getLevel();
		BlockPos plainAbs = helper.absolutePos(plain);
		BlockPos tankAbs = helper.absolutePos(withTank);
		for (Direction side : new Direction[] {null, Direction.UP, Direction.NORTH}) {
			helper.assertTrue(level.getCapability(BotaniaCapabilities.MANA_RECEIVER, plainAbs, side) == null,
					"plain backpack must not expose MANA_RECEIVER (side " + side + ")");
			helper.assertTrue(level.getCapability(BotaniaCapabilities.SPARK_ATTACHABLE, plainAbs, side) == null,
					"plain backpack must not expose SPARK_ATTACHABLE (side " + side + ")");
			ManaReceiver receiver = level.getCapability(BotaniaCapabilities.MANA_RECEIVER, tankAbs, side);
			helper.assertTrue(receiver instanceof ManaPool, "tank backpack must expose a ManaPool receiver (side " + side + ")");
			SparkAttachable attachable = level.getCapability(BotaniaCapabilities.SPARK_ATTACHABLE, tankAbs, side);
			helper.assertTrue(attachable != null && attachable.canAttachSpark(ItemStack.EMPTY), "tank backpack must be spark attachable");
		}
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void capabilitiesFollowUpgradeInstallAndRemoval(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, rel, TestSupport.newBackpack());
		BlockPos abs = helper.absolutePos(rel);
		var level = helper.getLevel();
		helper.assertTrue(level.getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) == null, "starts without capability");

		// Cached lookups (like Botania spreaders hold) must follow upgrade changes
		var cache = net.neoforged.neoforge.capabilities.BlockCapabilityCache.create(BotaniaCapabilities.MANA_RECEIVER, level, abs, null);
		helper.assertTrue(cache.getCapability() == null, "cache starts empty");

		int slot = TestSupport.install(be.getBackpackWrapper(), ModItems.MANA_TANK_UPGRADE.get());
		helper.assertTrue(level.getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) != null, "capability appears after installing the tank");
		helper.assertTrue(cache.getCapability() != null, "cached lookup must see the newly installed tank");

		be.getBackpackWrapper().getUpgradeHandler().setStackInSlot(slot, ItemStack.EMPTY);
		helper.assertTrue(level.getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null) == null, "capability disappears after removing the tank");
		helper.assertTrue(cache.getCapability() == null, "cached lookup must see the tank removal");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void placedTankReceivesBurstManaAndRespectsMode(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, rel, TestSupport.backpackWithTank(0));
		BlockPos abs = helper.absolutePos(rel);
		ManaReceiver receiver = helper.getLevel().getCapability(BotaniaCapabilities.MANA_RECEIVER, abs, null);
		helper.assertTrue(receiver != null, "receiver present");
		helper.assertTrue(receiver.canReceiveManaFromBursts(), "BOTH accepts bursts");
		helper.assertTrue(!receiver.isFull(), "empty tank is not full");

		receiver.receiveMana(1000); // what ManaBurst does on collision
		helper.assertTrue(TestSupport.tank(be.getBackpackWrapper()).getMana() == 1000, "mana from burst stored in the upgrade");
		helper.assertTrue(receiver.getCurrentMana() == 1000, "pool reports stored mana");
		helper.assertTrue(((ManaPool) receiver).isOutputtingPower(), "BOTH outputs power");

		TestSupport.tank(be.getBackpackWrapper()).setTransferMode(ManaTransferMode.OUTPUT);
		helper.assertTrue(!receiver.canReceiveManaFromBursts(), "OUTPUT refuses bursts");
		TestSupport.tank(be.getBackpackWrapper()).setTransferMode(ManaTransferMode.INPUT);
		helper.assertTrue(receiver.canReceiveManaFromBursts() && !((ManaPool) receiver).isOutputtingPower(), "INPUT accepts but does not output");
		helper.assertTrue(receiver.getCurrentMana() == 0, "INPUT hides mana from collectors/spreaders pulling");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void placedTankJoinsAndLeavesTheManaNetwork(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, rel, TestSupport.backpackWithTank(500));
		var level = helper.getLevel();
		BlockPos abs = helper.absolutePos(rel);
		var network = BotaniaAPI.instance().getManaNetworkInstance();

		helper.runAfterDelay(5, () -> {
			Set<ManaPool> pools = network.getAllPoolsInWorld(level);
			boolean found = pools.stream().anyMatch(pool -> pool.getManaReceiverPos().equals(abs));
			helper.assertTrue(found, "ticking backpack should register as pool in the mana network");
			helper.assertTrue(network.getClosestPool(abs, level, 4) != null, "closest pool lookup finds it");

			// Removing the upgrade unregisters immediately
			var handler = be.getBackpackWrapper().getUpgradeHandler();
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				handler.setStackInSlot(slot, ItemStack.EMPTY);
			}
			helper.assertTrue(network.getAllPoolsInWorld(level).stream().noneMatch(pool -> pool.getManaReceiverPos().equals(abs)),
					"removing the tank must unregister the pool");

			// Re-adding registers again, then breaking the block lets the sweep remove it
			TestSupport.install(be.getBackpackWrapper(), ModItems.MANA_TANK_UPGRADE.get());
			helper.runAfterDelay(5, () -> {
				helper.assertTrue(network.getAllPoolsInWorld(level).stream().anyMatch(pool -> pool.getManaReceiverPos().equals(abs)),
						"re-installed tank registers again");
				helper.setBlock(rel, Blocks.AIR);
				helper.runAfterDelay(30, () -> {
					helper.assertTrue(network.getAllPoolsInWorld(level).stream().noneMatch(pool -> pool.getManaReceiverPos().equals(abs)),
							"broken backpack must be swept out of the mana network within 20 ticks");
					helper.succeed();
				});
			});
		});
	}

	@GameTest(template = TEMPLATE, batch = "levelUnload")
	public static void levelUnloadUnregistersPools(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		TestSupport.placeBackpack(helper, rel, TestSupport.backpackWithTank(0));
		var level = helper.getLevel();
		BlockPos abs = helper.absolutePos(rel);
		var network = BotaniaAPI.instance().getManaNetworkInstance();
		helper.runAfterDelay(5, () -> {
			helper.assertTrue(network.getAllPoolsInWorld(level).stream().anyMatch(pool -> pool.getManaReceiverPos().equals(abs)), "registered");
			ManaBackpackPool.onLevelUnload(level);
			helper.assertTrue(network.getAllPoolsInWorld(level).stream().noneMatch(pool -> pool.getManaReceiverPos().equals(abs)),
					"level unload hook unregisters pools of that level");
			helper.succeed();
		});
	}
}
