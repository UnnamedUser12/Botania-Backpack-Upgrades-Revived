package velrondevs.botaniabackpack.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;

public final class ModCreativeTabs {
	private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
			BotaniaBackpackUpgrades.MOD_ID);

	public static final String MAIN_TAB_TRANSLATION_KEY = "itemGroup." + BotaniaBackpackUpgrades.MOD_ID + ".main";

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_TABS.register("main",
			() -> CreativeModeTab.builder()
					.title(Component.translatable(MAIN_TAB_TRANSLATION_KEY))
					.icon(() -> new ItemStack(ModItems.MANA_TANK_UPGRADE.get()))
					.displayItems((parameters, output) -> {
						output.accept(ModItems.MANA_TANK_UPGRADE.get());
						output.accept(ModItems.MANA_SUPPLY_UPGRADE.get());
						output.accept(ModItems.MANA_INFUSION_UPGRADE.get());
						output.accept(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get());
					})
					.build());

	public static void register(IEventBus modBus) {
		CREATIVE_TABS.register(modBus);
	}

	private ModCreativeTabs() {}
}
