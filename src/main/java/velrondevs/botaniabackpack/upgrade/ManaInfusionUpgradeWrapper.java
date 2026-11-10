package velrondevs.botaniabackpack.upgrade;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IExtractResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IFilteredUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import velrondevs.botania.api.recipe.ManaInfusionRecipe;
import velrondevs.botania.api.recipe.StateIngredient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ManaInfusionUpgradeWrapper extends UpgradeWrapperBase<ManaInfusionUpgradeWrapper, ManaInfusionUpgradeItem>
		implements IInsertResponseUpgrade, IExtractResponseUpgrade, IFilteredUpgrade, ISlotChangeResponseUpgrade, ITickableUpgrade {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final FilterLogic filterLogic;
	private final Map<Integer, PendingInfusion> pendingInfusions = new LinkedHashMap<>();
	private final Map<Integer, AutomaticInsertBaseline> automaticInsertBaselines = new LinkedHashMap<>();
	private final InsertionJournal insertionJournal = new InsertionJournal();
	private boolean initialInventoryScanPending = true;
	private boolean processing;

	public ManaInfusionUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), ModCoreDataComponents.FILTER_ATTRIBUTES);
		filterLogic.setAllowByDefault(true);
		filterLogic.getFilterHandler().setOnSlotChange(ignored -> {
			if (shouldWorkInGUI()) {
				queueAllMatchingSlots();
			}
		});
	}

	@Override
	public FilterLogic getFilterLogic() {
		return filterLogic;
	}

	public int getSlotsInRow() {
		return upgradeItem.getSlotsInRow();
	}

	public boolean isAdvanced() {
		return upgradeItem.isAdvanced();
	}

	@Override
	public int onBeforeInsert(InventoryHandler inventoryHandler, int slot, ItemResource resource, int amount) {
		if (!processing) {
			automaticInsertBaselines.put(slot, new AutomaticInsertBaseline(resource, inventoryHandler.getStackInSlot(slot).getCount()));
		}
		return 0;
	}

	@Override
	public void onAfterInsert(InventoryHandler inventoryHandler, int slot, TransactionContext tx) {
		if (processing) {
			return;
		}
		AutomaticInsertBaseline baseline = automaticInsertBaselines.remove(slot);
		ItemStack current = inventoryHandler.getStackInSlot(slot);
		if (baseline == null || current.isEmpty() || !baseline.input().matches(current)) {
			deferUntilCommit(tx, () -> queueSlot(inventoryHandler, slot));
			return;
		}

		int insertedCount = current.getCount() - baseline.countBefore();
		if (insertedCount > 0) {
			ItemStack inserted = current.copy();
			deferUntilCommit(tx, () -> queueInsertedItems(slot, inserted, insertedCount));
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onAfterExtract(InventoryHandler inventoryHandler, int slot, ItemResource originalResource) {
		if (processing || !pendingInfusions.containsKey(slot)) {
			return;
		}
		TransactionContext tx = Transaction.getCurrentOpenedTransaction();
		if (tx == null) {
			reconcilePendingSlot(inventoryHandler, slot);
		} else {
			deferUntilCommit(tx, () -> {
				if (pendingInfusions.containsKey(slot)) {
					reconcilePendingSlot(inventoryHandler, slot);
				}
			});
		}
	}

	private void deferUntilCommit(TransactionContext tx, Runnable action) {
		insertionJournal.updateSnapshots(tx);
		insertionJournal.actions.add(action);
	}

	@Override
	public void onSlotChange(InventoryHandler inventoryHandler, int slot) {
		if (shouldWorkInGUI()) {
			queueSlot(inventoryHandler, slot);
		}
	}

	public void setShouldWorkInGUI(boolean shouldWorkInGUI) {
		if (shouldWorkInGUI() == shouldWorkInGUI) {
			return;
		}
		upgrade.set(ModCoreDataComponents.SHOULD_WORK_IN_GUI.get(), shouldWorkInGUI);
		save();
	}

	public boolean shouldWorkInGUI() {
		return upgrade.getOrDefault(ModCoreDataComponents.SHOULD_WORK_IN_GUI.get(), false);
	}

	@Override
	public void onAdded() {
		if (shouldWorkInGUI()) {
			queueAllMatchingSlots();
		}
	}

	public void queueAllMatchingSlots() {
		if (processing) {
			return;
		}
		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		for (int slot = 0; slot < inventory.size(); slot++) {
			queueSlot(inventory, slot);
		}
	}

	private void queueSlot(InventoryHandler inventory, int slot) {
		if (processing || slot < 0 || slot >= inventory.size()) {
			return;
		}

		ItemStack stack = inventory.getStackInSlot(slot);
		if (stack.isEmpty() || !filterLogic.matchesFilter(stack)) {
			pendingInfusions.remove(slot);
			return;
		}

		PendingInfusion pending = pendingInfusions.get(slot);
		if (pending == null || !ItemStack.isSameItemSameComponents(pending.input(), stack)) {
			pendingInfusions.put(slot, new PendingInfusion(stack.copyWithCount(1), stack.getCount(), stack.getCount()));
			return;
		}

		int countDifference = stack.getCount() - pending.lastObservedCount();
		pending.adjustRemaining(countDifference);
		pending.setLastObservedCount(stack.getCount());
		if (pending.remaining() == 0) {
			pendingInfusions.remove(slot);
		}
	}

	private void queueInsertedItems(int slot, ItemStack stack, int insertedCount) {
		if (!filterLogic.matchesFilter(stack)) {
			return;
		}

		PendingInfusion pending = pendingInfusions.get(slot);
		if (pending == null || !ItemStack.isSameItemSameComponents(pending.input(), stack)) {
			pendingInfusions.put(slot, new PendingInfusion(stack.copyWithCount(1), insertedCount, stack.getCount()));
			return;
		}

		pending.adjustRemaining(insertedCount);
		pending.setLastObservedCount(stack.getCount());
	}

	private void reconcilePendingSlot(InventoryHandler inventory, int slot) {
		PendingInfusion pending = pendingInfusions.get(slot);
		if (pending == null) {
			return;
		}
		ItemStack stack = inventory.getStackInSlot(slot);
		if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(pending.input(), stack)) {
			pendingInfusions.remove(slot);
			return;
		}
		int countDifference = stack.getCount() - pending.lastObservedCount();
		pending.adjustRemaining(countDifference);
		pending.setLastObservedCount(stack.getCount());
		if (pending.remaining() == 0) {
			pendingInfusions.remove(slot);
		}
	}

	@Override
	public void tick(@Nullable Entity entity, Level level, BlockPos pos) {
		if (level.isClientSide()) {
			return;
		}
		if (initialInventoryScanPending) {
			initialInventoryScanPending = false;
			if (shouldWorkInGUI()) {
				queueAllMatchingSlots();
			}
		}
		if (pendingInfusions.isEmpty()) {
			return;
		}

		boolean catalystRecipesEnabled = isAdvanced() && entity == null;
		BlockState catalystState = catalystRecipesEnabled ? level.getBlockState(pos.below()) : null;
		for (int slot : new ArrayList<>(pendingInfusions.keySet())) {
			processOne(slot, entity, level, catalystState, catalystRecipesEnabled);
		}
	}

	private void processOne(int slot, @Nullable Entity entity, Level level, @Nullable BlockState catalystState,
			boolean catalystRecipesEnabled) {
		PendingInfusion pending = pendingInfusions.get(slot);
		if (pending == null || pending.remaining() <= 0) {
			pendingInfusions.remove(slot);
			return;
		}

		InventoryHandler inventory = storageWrapper.getInventoryHandler();
		ItemStack input = inventory.getStackInSlot(slot);
		if (input.isEmpty() || !ItemStack.isSameItemSameComponents(pending.input(), input) || !filterLogic.matchesFilter(input)) {
			pendingInfusions.remove(slot);
			return;
		}
		pending.limitRemainingTo(input.getCount());

		RecipeLookup lookup = findRecipe(level, input, catalystState, catalystRecipesEnabled);
		if (lookup.holder() == null) {
			if (!lookup.shouldRetry()) {
				pendingInfusions.remove(slot);
			}
			return;
		}

		RecipeHolder<ManaInfusionRecipe> holder = lookup.holder();
		ManaInfusionRecipe recipe = holder.value();
		int manaCost = recipe.getManaToConsume();
		if (manaCost <= 0) {
			LOGGER.warn("Ignoring mana infusion recipe {} with non-positive mana cost {}", holder.id(), manaCost);
			pendingInfusions.remove(slot);
			return;
		}

		ManaTankUpgradeWrapper manaTank = findManaTank();
		if (manaTank == null || manaTank.getMana() < manaCost) {
			return;
		}

		ItemStack output = recipe.getRecipeOutput(level.registryAccess(), input);
		if (output.isEmpty()) {
			LOGGER.warn("Ignoring mana infusion recipe {} because it produced an empty result", holder.id());
			pendingInfusions.remove(slot);
			return;
		}

		if (!applyConversion(inventory, slot, input, output, manaTank, manaCost, holder)) {
			return;
		}

		pending.decrementRemaining();
		ItemStack current = inventory.getStackInSlot(slot);
		pending.setLastObservedCount(ItemStack.isSameItemSameComponents(pending.input(), current) ? current.getCount() : 0);
		if (pending.remaining() == 0) {
			pendingInfusions.remove(slot);
		}

		if (entity instanceof Player player) {
			player.triggerRecipeCrafted(holder, List.of(output));
			output.onCraftedBy(player, output.getCount());
		}
	}

	private boolean applyConversion(InventoryHandler inventory, int slot, ItemStack input, ItemStack output,
			ManaTankUpgradeWrapper manaTank, int manaCost, RecipeHolder<ManaInfusionRecipe> holder) {
		ItemResource inputResource = ItemResource.of(input);
		ItemResource outputResource = ItemResource.of(output);
		int outputCount = output.getCount();

		processing = true;
		try (Transaction tx = Transaction.openRoot()) {
			if (inventory.extract(slot, inputResource, 1, tx) < 1) {
				return false;
			}
			if (inventory.insert(outputResource, outputCount, tx) < outputCount) {
				return false;
			}
			if (!manaTank.tryConsumeMana(manaCost)) {
				LOGGER.error("Mana infusion recipe {} completed but its previously available {} mana could not be consumed", holder.id(), manaCost);
				return false;
			}
			tx.commit();
			return true;
		} finally {
			processing = false;
		}
	}

	@Nullable
	private ManaTankUpgradeWrapper findManaTank() {
		List<ManaTankUpgradeWrapper> manaTanks = storageWrapper.getUpgradeHandler().getTypeWrappers(ManaTankUpgradeItem.TYPE);
		return manaTanks.isEmpty() ? null : manaTanks.get(0);
	}

	private static RecipeLookup findRecipe(Level level, ItemStack input, @Nullable BlockState catalystState,
			boolean catalystRecipesEnabled) {
		RecipeType<ManaInfusionRecipe> recipeType = getManaInfusionRecipeType();
		if (recipeType == null) {
			return RecipeLookup.NO_MATCH;
		}

		RecipeHolder<ManaInfusionRecipe> nonCatalystRecipe = null;
		boolean hasUnmatchedCatalystRecipe = false;
		if (!(level instanceof ServerLevel serverLevel)) {
			return RecipeLookup.NO_MATCH;
		}
		for (RecipeHolder<ManaInfusionRecipe> holder : serverLevel.recipeAccess().recipeMap().byType(recipeType)) {
			ManaInfusionRecipe recipe = holder.value();
			if (!recipe.matches(input)) {
				continue;
			}

			StateIngredient catalyst = recipe.getRecipeCatalyst();
			if (catalyst == null) {
				if (nonCatalystRecipe == null) {
					nonCatalystRecipe = holder;
				}
			} else if (catalystRecipesEnabled) {
				if (catalystState != null && catalyst.test(catalystState)) {
					return new RecipeLookup(holder, false);
				}
				hasUnmatchedCatalystRecipe = true;
			}
		}

		if (nonCatalystRecipe != null) {
			return new RecipeLookup(nonCatalystRecipe, false);
		}
		return hasUnmatchedCatalystRecipe ? RecipeLookup.RETRY : RecipeLookup.NO_MATCH;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	private static RecipeType<ManaInfusionRecipe> getManaInfusionRecipeType() {
		RecipeType<?> recipeType = BuiltInRegistries.RECIPE_TYPE.getValue(ManaInfusionRecipe.TYPE_ID);
		return recipeType == null ? null : (RecipeType) recipeType;
	}

	private record RecipeLookup(@Nullable RecipeHolder<ManaInfusionRecipe> holder, boolean shouldRetry) {
		private static final RecipeLookup NO_MATCH = new RecipeLookup(null, false);
		private static final RecipeLookup RETRY = new RecipeLookup(null, true);
	}

	private final class InsertionJournal extends SnapshotJournal<List<Runnable>> {
		private List<Runnable> actions = new ArrayList<>();

		@Override
		protected List<Runnable> createSnapshot() {
			return new ArrayList<>(actions);
		}

		@Override
		protected void revertToSnapshot(List<Runnable> snapshot) {
			actions = snapshot;
		}

		@Override
		protected void onRootCommit(List<Runnable> originalState) {
			List<Runnable> committed = actions;
			actions = new ArrayList<>();
			committed.forEach(Runnable::run);
		}
	}

	private record AutomaticInsertBaseline(ItemResource input, int countBefore) {}

	private static final class PendingInfusion {
		private final ItemStack input;
		private int remaining;
		private int lastObservedCount;

		private PendingInfusion(ItemStack input, int remaining, int lastObservedCount) {
			this.input = input;
			this.remaining = remaining;
			this.lastObservedCount = lastObservedCount;
		}

		private ItemStack input() {
			return input;
		}

		private int remaining() {
			return remaining;
		}

		private int lastObservedCount() {
			return lastObservedCount;
		}

		private void setLastObservedCount(int count) {
			lastObservedCount = Math.max(0, count);
		}

		private void adjustRemaining(int difference) {
			long adjusted = (long) remaining + difference;
			remaining = (int) Math.max(0, Math.min(Integer.MAX_VALUE, adjusted));
		}

		private void limitRemainingTo(int count) {
			remaining = Math.min(remaining, Math.max(0, count));
		}

		private void decrementRemaining() {
			remaining = Math.max(0, remaining - 1);
		}
	}
}
