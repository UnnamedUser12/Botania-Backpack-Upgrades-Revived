package velrondevs.botaniabackpack.compat.botania;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModAttachments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import velrondevs.botania.api.BotaniaAPI;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.ManaItemsEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public final class BotaniaCapabilityEvents {
	private BotaniaCapabilityEvents() {}

	/** Mod-bus: registers the Botania capabilities on placed backpacks and on backpack items. */
	@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
	public static final class ModBus {
		@SubscribeEvent
		public static void registerCapabilities(RegisterCapabilitiesEvent event) {
			// Placed backpack: mana pool + spark attachable, only while a mana tank upgrade is installed.
			event.registerBlockEntity(BotaniaCapabilities.MANA_RECEIVER, ModBlocks.BACKPACK_TILE_TYPE.get(),
					(backpack, side) -> ManaBackpackPool.findUpgrade(backpack).isPresent()
							? backpack.getData(ModAttachments.MANA_POOL.get()) : null);
			event.registerBlockEntity(BotaniaCapabilities.SPARK_ATTACHABLE, ModBlocks.BACKPACK_TILE_TYPE.get(),
					(backpack, side) -> ManaBackpackPool.findUpgrade(backpack).isPresent()
							? backpack.getData(ModAttachments.MANA_POOL.get()) : null);

			// Carried backpack: ManaItem availability follows the installed upgrades (resolved on every lookup).
			for (Item item : BuiltInRegistries.ITEM) {
				if (item instanceof BackpackItem) {
					event.registerItem(BotaniaCapabilities.MANA_ITEM, (stack, ignored) -> {
						ManaBackpackItem manaItem = new ManaBackpackItem(stack);
						return manaItem.isAvailable() ? manaItem : null;
					}, item);
				}
			}
		}

		private ModBus() {}
	}

	/** Game-bus: mana item discovery for equipped backpacks and mana-network housekeeping. */
	@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
	public static final class GameBus {
		private static int sweepCounter;

		/**
		 * Adds backpacks from armor or modded equipment inventories without adding a
		 * second copy of backpacks that Botania already found in the main, offhand,
		 * or accessories inventories.
		 */
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void addEquippedBackpackManaItems(ManaItemsEvent event) {
			SeenManaItems seen = new SeenManaItems();
			markBotaniaAccessoriesSeen(event, seen);
			deduplicateExistingSources(event, seen);

			PlayerInventoryProvider.get().runOnBackpacks(event.getPlayer(), (backpack, handlerName, identifier, slot) -> {
				if (seen.mark(backpack) && backpack.getCapability(BotaniaCapabilities.MANA_ITEM) != null) {
					event.getItems().add(backpack);
				}
				return false;
			});
		}

		@SubscribeEvent
		public static void onServerTick(ServerTickEvent.Post event) {
			if (++sweepCounter >= 20) {
				sweepCounter = 0;
				ManaBackpackPool.sweepInvalidPools();
			}
		}

		@SubscribeEvent
		public static void onLevelUnload(LevelEvent.Unload event) {
			if (event.getLevel() instanceof net.minecraft.world.level.Level level) {
				ManaBackpackPool.onLevelUnload(level);
			}
		}

		private static void deduplicateExistingSources(ManaItemsEvent event, SeenManaItems seen) {
			Iterator<ItemStack> iterator = event.getItems().iterator();
			while (iterator.hasNext()) {
				if (!seen.mark(iterator.next())) {
					iterator.remove();
				}
			}
		}

		private static void markBotaniaAccessoriesSeen(ManaItemsEvent event, SeenManaItems seen) {
			Container accessories = BotaniaAPI.instance().getAccessoriesInventory(event.getPlayer());
			for (int slot = 0; slot < accessories.getContainerSize(); slot++) {
				seen.mark(accessories.getItem(slot));
			}
		}

		private GameBus() {}
	}

	private static final class SeenManaItems {
		private final Set<ItemStack> stacks = Collections.newSetFromMap(new IdentityHashMap<>());
		private final Set<UUID> backpackIds = new HashSet<>();

		private boolean mark(ItemStack stack) {
			if (stack.isEmpty() || !stacks.add(stack)) {
				return false;
			}
			if (!(stack.getItem() instanceof BackpackItem)) {
				return true;
			}
			return BackpackWrapper.fromStack(stack).getContentsUuid()
					.map(backpackIds::add)
					.orElse(true);
		}
	}
}
