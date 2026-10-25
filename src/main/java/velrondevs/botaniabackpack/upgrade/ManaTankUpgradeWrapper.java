package velrondevs.botaniabackpack.upgrade;

import velrondevs.botaniabackpack.util.ManaStorageMath;
import velrondevs.botaniabackpack.compat.botania.ManaBackpackPool;
import velrondevs.botaniabackpack.compat.botania.ManaItemTransferHelper;
import velrondevs.botaniabackpack.registry.ModDataComponents;
import velrondevs.botaniabackpack.registry.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IRenderedTankUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;

import javax.annotation.Nullable;

import java.util.function.Consumer;

public final class ManaTankUpgradeWrapper extends UpgradeWrapperBase<ManaTankUpgradeWrapper, ManaTankUpgradeItem>
		implements IRenderedTankUpgrade, ITickableUpgrade {
	private Consumer<TankRenderInfo> renderInfoUpdate = ignored -> {};

	public ManaTankUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		int storedMana = getStoredMana(upgrade);
		int clamped = ManaStorageMath.clamp(storedMana, getCapacity());
		if (storedMana != clamped) {
			upgrade.set(ModDataComponents.MANA.get(), clamped);
			save();
		}
	}

	public static int getStoredMana(ItemStack upgrade) {
		return upgrade.getOrDefault(ModDataComponents.MANA.get(), 0);
	}

	/** Mana is always read straight from the upgrade stack so it can never diverge from the stored data. */
	public int getMana() {
		return getStoredMana(upgrade);
	}

	public int getManaAvailableForOutput() {
		return getTransferMode().providesMana() ? getMana() : 0;
	}

	public int getCapacity() {
		return upgradeItem.getCapacity();
	}

	public int getAvailableSpace() {
		return getTransferMode().acceptsMana() ? Math.max(0, getCapacity() - getMana()) : 0;
	}

	public int getInternalAvailableSpace() {
		return Math.max(0, getCapacity() - getMana());
	}

	public boolean isFullForInput() {
		return !getTransferMode().acceptsMana() || getMana() >= getCapacity();
	}

	public void receiveMana(int amount) {
		ManaTransferMode transferMode = getTransferMode();
		if ((amount > 0 && !transferMode.acceptsMana()) || (amount < 0 && !transferMode.providesMana()) || amount == 0) {
			return;
		}

		setMana(ManaStorageMath.applyDelta(getMana(), amount, getCapacity()));
	}

	/**
	 * Stores as much of the supplied mana as possible without consulting the
	 * external network transfer mode.
	 *
	 * @return the amount actually stored
	 */
	public int receiveInternalMana(int maxAmount) {
		if (maxAmount <= 0) {
			return 0;
		}
		int accepted = Math.min(maxAmount, getInternalAvailableSpace());
		if (accepted > 0) {
			setMana(getMana() + accepted);
		}
		return accepted;
	}

	/**
	 * Extracts up to the requested amount without consulting the external
	 * network transfer mode.
	 *
	 * @return the amount actually extracted
	 */
	public int extractInternalMana(int maxAmount) {
		if (maxAmount <= 0) {
			return 0;
		}
		int mana = getMana();
		int extracted = Math.min(maxAmount, mana);
		if (extracted > 0) {
			setMana(mana - extracted);
		}
		return extracted;
	}

	/**
	 * Consumes mana for another upgrade in this backpack. Transfer mode only
	 * controls external Botania network I/O, so it intentionally does not block
	 * this internal use.
	 */
	public boolean tryConsumeMana(int amount) {
		if (amount <= 0 || getMana() < amount) {
			return false;
		}
		return extractInternalMana(amount) == amount;
	}

	/**
	 * Transfers mana between this tank and a single mana-holding cursor item.
	 * The current transfer mode resolves direction ambiguity for items that can
	 * both receive and export.
	 *
	 * @return the amount transferred, or zero if no compatible transfer exists
	 */
	public int interactWithManaItem(ItemStack itemStack) {
		return ManaItemTransferHelper.interact(this, storageWrapper.getWrappedStorageStack(), itemStack, getTransferMode());
	}

	public ManaTransferMode getTransferMode() {
		return upgrade.getOrDefault(ModDataComponents.TRANSFER_MODE.get(), ManaTransferMode.BOTH);
	}

	public void setTransferMode(ManaTransferMode transferMode) {
		if (getTransferMode() == transferMode) {
			return;
		}
		upgrade.set(ModDataComponents.TRANSFER_MODE.get(), transferMode);
		save();
	}

	@Override
	public void setTankRenderInfoUpdateCallback(Consumer<TankRenderInfo> updateTankRenderInfoCallback) {
		renderInfoUpdate = updateTankRenderInfoCallback;
	}

	@Override
	public void forceUpdateTankRenderInfo() {
		TankRenderInfo renderInfo = new TankRenderInfo();
		int mana = getMana();
		if (mana > 0) {
			renderInfo.setFluid(new FluidStack(ModFluids.MANA_DISPLAY.get(), 1));
			renderInfo.setFillRatio(Math.round((float) mana / getCapacity() * 10F) / 10F);
		}
		renderInfoUpdate.accept(renderInfo);
	}

	@Override
	public void tick(@Nullable Entity entity, Level level, BlockPos pos) {
		if (entity == null && !level.isClientSide) {
			ManaBackpackPool.tickPlacedBackpack(level, pos, this);
		}
	}

	@Override
	public void onBeforeRemoved() {
		ManaBackpackPool.onUpgradeRemoved(storageWrapper);
	}

	@Override
	public boolean canBeDisabled() {
		return false;
	}

	public IStorageWrapper getStorageWrapperIdentity() {
		return storageWrapper;
	}

	private void setMana(int updatedMana) {
		int clampedMana = ManaStorageMath.clamp(updatedMana, getCapacity());
		if (clampedMana == getMana()) {
			return;
		}
		upgrade.set(ModDataComponents.MANA.get(), clampedMana);
		save();
		forceUpdateTankRenderInfo();
	}
}
