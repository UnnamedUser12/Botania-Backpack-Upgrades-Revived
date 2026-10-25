package velrondevs.botaniabackpack.event;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
public final class CreativeTabEvents {
	@SubscribeEvent
	public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
		if (event.getTab() == net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CREATIVE_TAB.get()) {
			event.accept(ModItems.MANA_TANK_UPGRADE.get());
			event.accept(ModItems.MANA_SUPPLY_UPGRADE.get());
			event.accept(ModItems.MANA_INFUSION_UPGRADE.get());
			event.accept(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get());
		}
	}

	private CreativeTabEvents() {}
}
