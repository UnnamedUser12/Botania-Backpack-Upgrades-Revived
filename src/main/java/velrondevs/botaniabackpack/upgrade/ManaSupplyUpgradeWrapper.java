package velrondevs.botaniabackpack.upgrade;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;

import java.util.function.Consumer;

/**
 * Marker wrapper used to enable portable mana supply. It deliberately has no
 * tick method: mana is consumed only when Botania requests it.
 */
public final class ManaSupplyUpgradeWrapper extends UpgradeWrapperBase<ManaSupplyUpgradeWrapper, ManaSupplyUpgradeItem> {
	public ManaSupplyUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
	}

	@Override
	public boolean hideSettingsTab() {
		return true;
	}
}
