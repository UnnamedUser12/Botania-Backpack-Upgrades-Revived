package velrondevs.botaniabackpack.upgrade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicContainer;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

public final class ManaInfusionUpgradeContainer extends UpgradeContainerBase<ManaInfusionUpgradeWrapper, ManaInfusionUpgradeContainer> {
	private static final String DATA_SHOULD_WORK_IN_GUI = "shouldWorkInGUI";

	public static final UpgradeContainerType<ManaInfusionUpgradeWrapper, ManaInfusionUpgradeContainer> BASIC_TYPE =
			new UpgradeContainerType<>(ManaInfusionUpgradeContainer::new);
	public static final UpgradeContainerType<ManaInfusionUpgradeWrapper, ManaInfusionUpgradeContainer> ADVANCED_TYPE =
			new UpgradeContainerType<>(ManaInfusionUpgradeContainer::new);

	private final FilterLogicContainer<FilterLogic> filterLogicContainer;

	private ManaInfusionUpgradeContainer(Player player, int containerId, ManaInfusionUpgradeWrapper wrapper,
			UpgradeContainerType<ManaInfusionUpgradeWrapper, ManaInfusionUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
		filterLogicContainer = new FilterLogicContainer<>(() -> upgradeWrapper.getFilterLogic(), this, slots::add);
	}

	@Override
	public void handlePacket(CompoundTag data) {
		if (data.contains(DATA_SHOULD_WORK_IN_GUI)) {
			setShouldWorkInGUI(data.getBoolean(DATA_SHOULD_WORK_IN_GUI));
		}
		filterLogicContainer.handlePacket(data);
		if (upgradeWrapper.shouldWorkInGUI()) {
			upgradeWrapper.queueAllMatchingSlots();
		}
	}

	public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
		return filterLogicContainer;
	}

	public void setShouldWorkInGUI(boolean shouldWorkInGUI) {
		upgradeWrapper.setShouldWorkInGUI(shouldWorkInGUI);
		if (shouldWorkInGUI) {
			upgradeWrapper.queueAllMatchingSlots();
		}
		sendDataToServer(() -> NBTHelper.putBoolean(new CompoundTag(), DATA_SHOULD_WORK_IN_GUI, shouldWorkInGUI));
	}

	public boolean shouldWorkInGUI() {
		return upgradeWrapper.shouldWorkInGUI();
	}
}
