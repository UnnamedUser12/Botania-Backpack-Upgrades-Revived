package velrondevs.botaniabackpack.compat.botania;

import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;
import net.minecraft.world.item.ItemStack;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.ManaItem;

/**
 * Server-side transfers between one mana tank and an item capability.
 *
 * <p>Every transfer observes the target after calling {@link ManaItem#addMana}
 * and only commits the amount the item actually accepted or released. This
 * keeps the tank consistent with capability implementations that clamp values
 * more aggressively than their reported bounds.</p>
 */
public final class ManaItemTransferHelper {
	public static int interact(ManaTankUpgradeWrapper tank, ItemStack backpack, ItemStack itemStack, ManaTransferMode mode) {
		if (backpack.isEmpty() || itemStack.isEmpty() || itemStack.getCount() != 1 || itemStack == backpack) {
			return 0;
		}
		ManaItem manaItem = itemStack.getCapability(BotaniaCapabilities.MANA_ITEM);
		if (manaItem == null) {
			return 0;
		}

		return switch (mode) {
			case INPUT -> transferItemToTank(tank, backpack, manaItem);
			case OUTPUT -> transferTankToItem(tank, backpack, manaItem);
			case BOTH -> {
				int transferred = tank.getMana() > 0 ? transferTankToItem(tank, backpack, manaItem) : 0;
				yield transferred > 0 ? transferred : transferItemToTank(tank, backpack, manaItem);
			}
		};
	}

	private static int transferTankToItem(ManaTankUpgradeWrapper tank, ItemStack backpack, ManaItem manaItem) {
		if (tank.getMana() <= 0 || !manaItem.canReceiveManaFromItem(backpack)) {
			return 0;
		}

		int manaBefore = nonNegative(manaItem.getMana());
		int space = availableSpace(manaBefore, manaItem.getMaxMana());
		int requested = Math.min(tank.getMana(), space);
		if (requested <= 0) {
			return 0;
		}

		int reserved = tank.extractInternalMana(requested);
		if (reserved <= 0) {
			return 0;
		}

		RuntimeException failure = null;
		try {
			manaItem.addMana(reserved);
		} catch (RuntimeException exception) {
			failure = exception;
		}

		int accepted = observedIncrease(manaBefore, manaItem, reserved);
		if (accepted < reserved) {
			tank.receiveInternalMana(reserved - accepted);
		}
		if (failure != null) {
			throw failure;
		}
		return accepted;
	}

	private static int transferItemToTank(ManaTankUpgradeWrapper tank, ItemStack backpack, ManaItem manaItem) {
		if (tank.getInternalAvailableSpace() <= 0 || !manaItem.canExportManaToItem(backpack)) {
			return 0;
		}

		int manaBefore = nonNegative(manaItem.getMana());
		int requested = Math.min(manaBefore, tank.getInternalAvailableSpace());
		if (requested <= 0) {
			return 0;
		}

		RuntimeException failure = null;
		try {
			manaItem.addMana(-requested);
		} catch (RuntimeException exception) {
			failure = exception;
		}

		int released = observedDecrease(manaBefore, manaItem, requested);
		int accepted = tank.receiveInternalMana(released);
		if (accepted < released) {
			manaItem.addMana(released - accepted);
		}
		if (failure != null) {
			throw failure;
		}
		return accepted;
	}

	private static int availableSpace(int stored, int capacity) {
		long space = (long) Math.max(0, capacity) - stored;
		return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, space));
	}

	private static int observedIncrease(int before, ManaItem manaItem, int limit) {
		long difference = (long) nonNegative(manaItem.getMana()) - before;
		return (int) Math.max(0L, Math.min(limit, difference));
	}

	private static int observedDecrease(int before, ManaItem manaItem, int limit) {
		long difference = (long) before - nonNegative(manaItem.getMana());
		return (int) Math.max(0L, Math.min(limit, difference));
	}

	private static int nonNegative(int mana) {
		return Math.max(0, mana);
	}

	private ManaItemTransferHelper() {}
}
