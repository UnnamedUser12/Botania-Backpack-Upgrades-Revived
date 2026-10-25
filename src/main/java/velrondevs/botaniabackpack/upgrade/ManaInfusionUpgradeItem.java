package velrondevs.botaniabackpack.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.List;
import java.util.function.IntSupplier;

public final class ManaInfusionUpgradeItem extends UpgradeItemBase<ManaInfusionUpgradeWrapper> {
	public static final UpgradeType<ManaInfusionUpgradeWrapper> TYPE = new UpgradeType<>(ManaInfusionUpgradeWrapper::new);

	private final boolean advanced;
	private final IntSupplier filterSlotCount;
	private final IntSupplier slotsInRow;

	public ManaInfusionUpgradeItem(boolean advanced, IntSupplier filterSlotCount, IntSupplier slotsInRow,
			IUpgradeCountLimitConfig upgradeCountLimitConfig) {
		super(upgradeCountLimitConfig);
		this.advanced = advanced;
		this.filterSlotCount = filterSlotCount;
		this.slotsInRow = slotsInRow;
	}

	@Override
	public UpgradeType<ManaInfusionUpgradeWrapper> getType() {
		return TYPE;
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return List.of(new UpgradeConflictDefinition(ManaInfusionUpgradeItem.class::isInstance, 0,
				Component.translatable("gui.botaniabackpack.status.add.mana_infusion_exists")));
	}

	public boolean isAdvanced() {
		return advanced;
	}

	public int getFilterSlotCount() {
		return filterSlotCount.getAsInt();
	}

	public int getSlotsInRow() {
		return slotsInRow.getAsInt();
	}
}
