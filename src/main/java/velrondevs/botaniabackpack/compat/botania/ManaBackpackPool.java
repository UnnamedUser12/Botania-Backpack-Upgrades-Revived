package velrondevs.botaniabackpack.compat.botania;

import velrondevs.botaniabackpack.registry.ModAttachments;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import velrondevs.botania.api.BotaniaAPI;
import velrondevs.botania.api.mana.ManaBlockType;
import velrondevs.botania.api.mana.ManaNetworkAction;
import velrondevs.botania.api.mana.ManaPool;
import velrondevs.botania.api.mana.spark.ManaSpark;
import velrondevs.botania.api.mana.spark.SparkAttachable;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Capability adapter between a placed Sophisticated Backpack and Botania's mana
 * APIs. The authoritative mana state remains in the upgrade stack.
 *
 * <p>One instance lives on each backpack block entity as a data attachment. It joins the Botania
 * mana network while a mana tank upgrade is installed and the backpack is placed in a loaded level.</p>
 */
public final class ManaBackpackPool implements ManaPool, SparkAttachable {
	/** Pools currently registered in Botania's mana network. Swept regularly for removed/unloaded backpacks. */
	private static final List<ManaBackpackPool> REGISTERED_POOLS = new CopyOnWriteArrayList<>();

	private final BackpackBlockEntity backpack;
	@Nullable
	private IStorageWrapper boundStorage;
	@Nullable
	private Level registeredLevel;
	private boolean registered;

	public ManaBackpackPool(BackpackBlockEntity backpack) {
		this.backpack = backpack;
	}

	public static Optional<ManaTankUpgradeWrapper> findUpgrade(BackpackBlockEntity backpack) {
		return backpack.getBackpackWrapper().getUpgradeHandler().getSlotWrappers().values().stream()
				.filter(ManaTankUpgradeWrapper.class::isInstance)
				.map(ManaTankUpgradeWrapper.class::cast)
				.filter(ManaTankUpgradeWrapper::isEnabled)
				.findFirst();
	}

	public static void tickPlacedBackpack(Level level, BlockPos pos, ManaTankUpgradeWrapper wrapper) {
		if (level.getBlockEntity(pos) instanceof BackpackBlockEntity backpack) {
			backpack.getData(ModAttachments.MANA_POOL.get()).ensureRegistered(wrapper);
		}
	}

	public static void onUpgradeRemoved(IStorageWrapper storageWrapper) {
		for (ManaBackpackPool pool : new ArrayList<>(REGISTERED_POOLS)) {
			if (pool.boundStorage == storageWrapper) {
				pool.boundStorage = null;
				pool.unregister();
			}
		}
	}

	/** Unregisters pools whose backpack was broken, replaced or unloaded. */
	public static void sweepInvalidPools() {
		for (ManaBackpackPool pool : new ArrayList<>(REGISTERED_POOLS)) {
			if (!pool.isBackpackStillPlaced()) {
				pool.boundStorage = null;
				pool.unregister();
			}
		}
	}

	public static void onLevelUnload(Level level) {
		for (ManaBackpackPool pool : new ArrayList<>(REGISTERED_POOLS)) {
			if (pool.registeredLevel == level) {
				pool.boundStorage = null;
				pool.unregister();
			}
		}
	}

	private boolean isBackpackStillPlaced() {
		Level level = backpack.getLevel();
		return level != null && !backpack.isRemoved() && level.getBlockEntity(backpack.getBlockPos()) == backpack;
	}

	private void ensureRegistered(ManaTankUpgradeWrapper wrapper) {
		Level level = backpack.getLevel();
		if (level == null || level.isClientSide || backpack.isRemoved()) {
			return;
		}

		boundStorage = wrapper.getStorageWrapperIdentity();

		if (!registered) {
			registeredLevel = level;
			BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.ADD);
			registered = true;
			REGISTERED_POOLS.add(this);
		}
	}

	private void unregister() {
		REGISTERED_POOLS.remove(this);
		if (!registered || registeredLevel == null || registeredLevel.isClientSide) {
			registered = false;
			registeredLevel = null;
			return;
		}
		BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.POOL, ManaNetworkAction.REMOVE);
		registered = false;
		registeredLevel = null;
	}

	@Override
	public Level getManaReceiverLevel() {
		if (registeredLevel != null) {
			return registeredLevel;
		}
		return Objects.requireNonNull(backpack.getLevel(), "Mana backpack is not attached to a level");
	}

	@Override
	public BlockPos getManaReceiverPos() {
		return backpack.getBlockPos();
	}

	@Override
	public int getCurrentMana() {
		return findUpgrade(backpack).map(ManaTankUpgradeWrapper::getManaAvailableForOutput).orElse(0);
	}


	@Override
	public boolean isFull() {
		return findUpgrade(backpack).map(ManaTankUpgradeWrapper::isFullForInput).orElse(true);
	}

	@Override
	public void receiveMana(int mana) {
		findUpgrade(backpack).ifPresent(upgrade -> upgrade.receiveMana(mana));
	}

	@Override
	public boolean canReceiveManaFromBursts() {
		return !backpack.isRemoved() && findUpgrade(backpack).map(upgrade -> upgrade.getTransferMode().acceptsMana()).orElse(false);
	}

	@Override
	public boolean isOutputtingPower() {
		return findUpgrade(backpack).map(upgrade -> upgrade.getTransferMode().providesMana()).orElse(false);
	}

	@Override
	public int getMaxMana() {
		return findUpgrade(backpack).map(ManaTankUpgradeWrapper::getCapacity).orElse(1);
	}

	@Override
	public Optional<DyeColor> getColor() {
		return Optional.empty();
	}

	@Override
	public void setColor(Optional<DyeColor> color) {
		// Backpack color and spark network color are controlled independently.
	}

	@Override
	public boolean canAttachSpark(ItemStack stack) {
		return findUpgrade(backpack).isPresent();
	}

	@Override
	public int getAvailableSpaceForMana() {
		return findUpgrade(backpack).map(ManaTankUpgradeWrapper::getAvailableSpace).orElse(0);
	}

	@Override
	@Nullable
	public ManaSpark getAttachedSpark() {
		Level level = backpack.getLevel();
		if (level == null) {
			return null;
		}
		List<Entity> sparks = level.getEntitiesOfClass(Entity.class, new AABB(backpack.getBlockPos().above()),
				Predicates.instanceOf(ManaSpark.class));
		return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
	}

	@Override
	public boolean areIncomingTranfersDone() {
		return getAvailableSpaceForMana() <= 0;
	}
}
