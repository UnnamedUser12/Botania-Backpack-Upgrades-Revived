package velrondevs.botaniabackpack.upgrade;

import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeCountLimitConfig;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.List;

/**
 * Enables the mana tank in the same backpack as a portable Botania mana
 * source. The actual capability is attached to the backpack stack so Botania
 * can discover it through its normal player-inventory lookup.
 */
public final class ManaSupplyUpgradeItem extends UpgradeItemBase<ManaSupplyUpgradeWrapper> {
	public static final UpgradeType<ManaSupplyUpgradeWrapper> TYPE = new UpgradeType<>(ManaSupplyUpgradeWrapper::new);

	public ManaSupplyUpgradeItem(IUpgradeCountLimitConfig upgradeCountLimitConfig) {
		super(upgradeCountLimitConfig);
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
