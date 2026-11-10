package velrondevs.botaniabackpack.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.List;

public final class ManaSupplyUpgradeItem extends UpgradeItemBase<ManaSupplyUpgradeWrapper> {
	public static final UpgradeType<ManaSupplyUpgradeWrapper> TYPE = new UpgradeType<>(ManaSupplyUpgradeWrapper::new);

	public ManaSupplyUpgradeItem(IUpgradeCountLimitConfig upgradeCountLimitConfig, Properties properties) {
		super(upgradeCountLimitConfig, properties);
	}

	@Override
	public UpgradeType<ManaSupplyUpgradeWrapper> getType() {
		return TYPE;
	}

	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return List.of(new UpgradeConflictDefinition(ManaSupplyUpgradeItem.class::isInstance, 0,
				Component.translatable("gui.botaniabackpack.status.add.mana_supply_exists")));
	}
}
