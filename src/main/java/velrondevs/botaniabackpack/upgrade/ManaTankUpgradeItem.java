package velrondevs.botaniabackpack.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.List;
import java.util.function.IntSupplier;

public final class ManaTankUpgradeItem extends UpgradeItemBase<ManaTankUpgradeWrapper> {
	public static final UpgradeType<ManaTankUpgradeWrapper> TYPE = new UpgradeType<>(ManaTankUpgradeWrapper::new);

	private final IntSupplier capacitySupplier;

	public ManaTankUpgradeItem(IntSupplier capacitySupplier, IUpgradeCountLimitConfig upgradeCountLimitConfig) {
		super(upgradeCountLimitConfig);
		this.capacitySupplier = capacitySupplier;
	}

	@Override
	public UpgradeType<ManaTankUpgradeWrapper> getType() {
		return TYPE;
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return List.of(new UpgradeConflictDefinition(ManaTankUpgradeItem.class::isInstance, 0,
				Component.translatable("gui.botaniabackpack.status.add.mana_tank_exists")));
	}

	@Override
	public int getInventoryColumnsTaken() {
		return 2;
	}

	public int getCapacity() {
		return Math.max(1, capacitySupplier.getAsInt());
	}
}
