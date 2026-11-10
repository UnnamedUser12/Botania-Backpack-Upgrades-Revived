package velrondevs.botaniabackpack.registry;

import velrondevs.botaniabackpack.config.BotaniaBackpackConfig;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaSupplyUpgradeItem;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;

public final class ModItems {
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BotaniaBackpackUpgrades.MOD_ID);

	public static final DeferredHolder<Item, ManaTankUpgradeItem> MANA_TANK_UPGRADE = ITEMS.registerItem("mana_tank_upgrade",
			properties -> new ManaTankUpgradeItem(BotaniaBackpackConfig.safe(BotaniaBackpackConfig.MANA_CAPACITY), Config.SERVER.maxUpgradesPerStorage, properties));
	public static final DeferredHolder<Item, ManaSupplyUpgradeItem> MANA_SUPPLY_UPGRADE = ITEMS.registerItem("mana_supply_upgrade",
			properties -> new ManaSupplyUpgradeItem(Config.SERVER.maxUpgradesPerStorage, properties));
	public static final DeferredHolder<Item, ManaInfusionUpgradeItem> MANA_INFUSION_UPGRADE = ITEMS.registerItem("mana_infusion_upgrade",
			properties -> new ManaInfusionUpgradeItem(false, BotaniaBackpackConfig.safe(BotaniaBackpackConfig.MANA_INFUSION_FILTER_SLOTS),
					BotaniaBackpackConfig.safe(BotaniaBackpackConfig.MANA_INFUSION_SLOTS_IN_ROW), Config.SERVER.maxUpgradesPerStorage, properties));
	public static final DeferredHolder<Item, ManaInfusionUpgradeItem> ADVANCED_MANA_INFUSION_UPGRADE = ITEMS.registerItem("advanced_mana_infusion_upgrade",
			properties -> new ManaInfusionUpgradeItem(true, BotaniaBackpackConfig.safe(BotaniaBackpackConfig.ADVANCED_MANA_INFUSION_FILTER_SLOTS),
					BotaniaBackpackConfig.safe(BotaniaBackpackConfig.ADVANCED_MANA_INFUSION_SLOTS_IN_ROW), Config.SERVER.maxUpgradesPerStorage, properties));

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
	}

	private ModItems() {}
}
