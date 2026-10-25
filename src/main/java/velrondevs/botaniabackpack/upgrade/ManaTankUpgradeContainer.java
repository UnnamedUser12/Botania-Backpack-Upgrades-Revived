package velrondevs.botaniabackpack.upgrade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;

public final class ManaTankUpgradeContainer extends UpgradeContainerBase<ManaTankUpgradeWrapper, ManaTankUpgradeContainer> {
	private static final String DATA_TRANSFER_MODE = "transferMode";
	private static final String DATA_CURSOR_INTERACTION = "cursorInteraction";

	public static final UpgradeContainerType<ManaTankUpgradeWrapper, ManaTankUpgradeContainer> TYPE =
			new UpgradeContainerType<>(ManaTankUpgradeContainer::new);

	private ManaTankUpgradeContainer(Player player, int containerId, ManaTankUpgradeWrapper wrapper,
			UpgradeContainerType<ManaTankUpgradeWrapper, ManaTankUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
	}

	public ManaTransferMode getTransferMode() {
		return upgradeWrapper.getTransferMode();
	}

	public void setTransferMode(ManaTransferMode mode) {
		upgradeWrapper.setTransferMode(mode);
		sendDataToServer(() -> {
			CompoundTag data = new CompoundTag();
			data.putString(DATA_TRANSFER_MODE, mode.getSerializedName());
			return data;
		});
	}

	public int getMana() {
		return upgradeWrapper.getMana();
	}

	public int getCapacity() {
		return upgradeWrapper.getCapacity();
	}

	public void requestCursorInteraction() {
		sendDataToServer(() -> {
			CompoundTag data = new CompoundTag();
			data.putBoolean(DATA_CURSOR_INTERACTION, true);
			return data;
		});
	}

	@Override
	public void handlePacket(CompoundTag data) {
		if (data.contains(DATA_TRANSFER_MODE)) {
			setTransferMode(ManaTransferMode.fromName(data.getString(DATA_TRANSFER_MODE)));
		}
		if (data.getBoolean(DATA_CURSOR_INTERACTION)) {
			handleCursorInteraction();
		}
	}

	private void handleCursorInteraction() {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return;
		}
		AbstractContainerMenu menu = serverPlayer.containerMenu;
		ItemStack cursorStack = menu.getCarried();
		if (cursorStack.getCount() != 1 || upgradeWrapper.interactWithManaItem(cursorStack) <= 0) {
			return;
		}

		// ManaItem capabilities usually mutate their owning stack in place. Force
		// an explicit carried-stack update so the client receives the new data
		// immediately instead of waiting for the next menu synchronization pass.
		menu.setCarried(cursorStack);
		serverPlayer.connection.send(
				new ClientboundContainerSetSlotPacket(-1, menu.incrementStateId(), -1, menu.getCarried().copy()));
	}
}
