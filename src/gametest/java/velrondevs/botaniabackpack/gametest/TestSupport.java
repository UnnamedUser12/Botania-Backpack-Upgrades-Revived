package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.ManaItem;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeWrapper;
import velrondevs.botaniabackpack.upgrade.ManaSupplyUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;

import java.util.List;

/** Shared helpers for the GameTests. */
final class TestSupport {
	static final BlockPos ORIGIN = new BlockPos(1, 1, 1);

	private TestSupport() {}

	static Item botaniaItem(String path) {
		Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("botania", path));
		if (item == Items.AIR) {
			throw new IllegalStateException("Botania item not found: " + path);
		}
		return item;
	}

	/** A fresh backpack stack whose storage already has a contents UUID (needed before upgrades can be installed). */
	static ItemStack newBackpack() {
		ItemStack stack = new ItemStack(net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.DIAMOND_BACKPACK.get());
		BackpackWrapper.fromStack(stack).getInventoryHandler();
		return stack;
	}

	static IBackpackWrapper wrapper(ItemStack backpack) {
		return BackpackWrapper.fromStack(backpack);
	}

	/** Installs an upgrade in the first free upgrade slot and returns that slot. */
	static int install(IStorageWrapper wrapper, Item upgrade) {
		var handler = wrapper.getUpgradeHandler();
		for (int slot = 0; slot < handler.getSlots(); slot++) {
			if (handler.getStackInSlot(slot).isEmpty()) {
				ItemStack remainder = handler.insertItem(slot, new ItemStack(upgrade), false);
				if (!remainder.isEmpty()) {
					throw new IllegalStateException("Upgrade " + upgrade + " was rejected by slot " + slot);
				}
				return slot;
			}
		}
		throw new IllegalStateException("No free upgrade slot");
	}

	static ManaTankUpgradeWrapper tank(IStorageWrapper wrapper) {
		List<ManaTankUpgradeWrapper> tanks = wrapper.getUpgradeHandler().getTypeWrappers(ManaTankUpgradeItem.TYPE);
		if (tanks.isEmpty()) {
			throw new IllegalStateException("No mana tank installed");
		}
		return tanks.get(0);
	}

	static ManaInfusionUpgradeWrapper infusion(IStorageWrapper wrapper) {
		List<ManaInfusionUpgradeWrapper> list = wrapper.getUpgradeHandler().getTypeWrappers(ManaInfusionUpgradeItem.TYPE);
		if (list.isEmpty()) {
			throw new IllegalStateException("No infusion upgrade installed");
		}
		return list.get(0);
	}

	static boolean hasSupply(IStorageWrapper wrapper) {
		return wrapper.getUpgradeHandler().hasUpgrade(ManaSupplyUpgradeItem.TYPE);
	}

	/** Backpack with the Mana Tank (and optionally more upgrades) already containing the given mana. */
	static ItemStack backpackWithTank(int mana) {
		ItemStack backpack = newBackpack();
		IBackpackWrapper wrapper = wrapper(backpack);
		install(wrapper, velrondevs.botaniabackpack.registry.ModItems.MANA_TANK_UPGRADE.get());
		if (mana > 0) {
			tank(wrapper).receiveInternalMana(mana);
		}
		return backpack;
	}

	static BackpackBlockEntity placeBackpack(GameTestHelper helper, BlockPos relative, ItemStack backpack) {
		helper.setBlock(relative, ModBlocks.BACKPACK.get().defaultBlockState());
		BackpackBlockEntity be = (BackpackBlockEntity) helper.getBlockEntity(relative);
		be.setBackpack(backpack);
		return be;
	}

	static int interact(ManaTankUpgradeWrapper tank, ItemStack item, velrondevs.botaniabackpack.upgrade.ManaTransferMode mode) {
		return velrondevs.botaniabackpack.compat.botania.ManaItemTransferHelper.interact(tank, tank.getStorageWrapperIdentity().getWrappedStorageStack(), item, mode);
	}

	/**
	 * Server player with a connection that swallows packets. GameTestHelper.makeMockServerPlayerInLevel() joins the player
	 * list with an unconfigured connection, which makes Curios fail with "Payload curios:sync_data may not be sent".
	 */
	static net.minecraft.server.level.ServerPlayer mockPlayer(GameTestHelper helper) {
		var server = helper.getLevel().getServer();
		var cookie = net.minecraft.server.network.CommonListenerCookie.createInitial(
				new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "test-mock-player"), false);
		var player = new net.minecraft.server.level.ServerPlayer(server, helper.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
			@Override
			public boolean isSpectator() {
				return false;
			}

			@Override
			public boolean isCreative() {
				return false;
			}
		};
		var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
		new io.netty.channel.embedded.EmbeddedChannel(connection);
		player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
				// packets are irrelevant in the tests
			}
		};
		player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 1, 1.5)));
		return player;
	}

	static ManaItem manaItem(ItemStack stack) {
		return stack.getCapability(BotaniaCapabilities.MANA_ITEM);
	}
}
