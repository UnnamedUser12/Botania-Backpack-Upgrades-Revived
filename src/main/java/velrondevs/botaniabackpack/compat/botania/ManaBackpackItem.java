package velrondevs.botaniabackpack.compat.botania;

import velrondevs.botaniabackpack.upgrade.ManaSupplyUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import velrondevs.botania.api.mana.ManaItem;

import java.util.List;
import java.util.Optional;

public final class ManaBackpackItem implements ManaItem {
	private final ItemStack backpack;

	public ManaBackpackItem(ItemStack backpack) {
		this.backpack = backpack;
	}

	public boolean isAvailable() {
		return findManaTank().isPresent();
	}

	@Override
	public int getMana() {
		return findManaTank().map(ManaTankUpgradeWrapper::getMana).orElse(0);
	}

	@Override
	public int getMaxMana() {

		return findManaTank().map(ManaTankUpgradeWrapper::getCapacity).orElse(1);
	}

	@Override
	public void addMana(int mana) {
		if (mana >= 0) {
			return;
		}
		int requested = (int) Math.min(Integer.MAX_VALUE, -(long) mana);
		findManaTank().ifPresent(tank -> tank.extractInternalMana(requested));
	}

	@Override
	public boolean canReceiveManaFromPool(BlockEntity pool) {
		return false;
	}

	@Override
	public boolean canReceiveManaFromItem(ItemStack otherStack) {
		return false;
	}

	@Override
	public boolean canExportManaToPool(BlockEntity pool) {
		return false;
	}

	@Override
	public boolean canExportManaToItem(ItemStack otherStack) {
		return !otherStack.isEmpty() && isAvailable();
	}

	@Override
	public boolean isNoExport() {
		return !isAvailable();
	}

	private Optional<ManaTankUpgradeWrapper> findManaTank() {
		if (backpack.isEmpty() || backpack.getCount() != 1 || !(backpack.getItem() instanceof BackpackItem)
				|| !backpack.has(ModCoreDataComponents.STORAGE_UUID.get())) {
			return Optional.empty();
		}
		IStorageWrapper wrapper = BackpackWrapper.fromStack(backpack);
		return Optional.of(wrapper)
				.filter(ManaBackpackItem::hasManaSupplyUpgrade)
				.flatMap(ManaBackpackItem::getManaTank);
	}

	private static boolean hasManaSupplyUpgrade(IStorageWrapper storageWrapper) {
		return storageWrapper.getUpgradeHandler().hasUpgrade(ManaSupplyUpgradeItem.TYPE);
	}

	private static Optional<ManaTankUpgradeWrapper> getManaTank(IStorageWrapper storageWrapper) {
		List<ManaTankUpgradeWrapper> manaTanks = storageWrapper.getUpgradeHandler().getTypeWrappers(ManaTankUpgradeItem.TYPE);
		return manaTanks.isEmpty() ? Optional.empty() : Optional.of(manaTanks.get(0));
	}
}
