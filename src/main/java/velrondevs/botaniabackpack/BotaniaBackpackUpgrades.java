package velrondevs.botaniabackpack;

import velrondevs.botaniabackpack.config.BotaniaBackpackConfig;
import velrondevs.botaniabackpack.registry.ModAttachments;
import velrondevs.botaniabackpack.registry.ModDataComponents;
import velrondevs.botaniabackpack.registry.ModFluidTypes;
import velrondevs.botaniabackpack.registry.ModFluids;
import velrondevs.botaniabackpack.registry.ModCreativeTabs;
import velrondevs.botaniabackpack.registry.ModItems;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;

@Mod(BotaniaBackpackUpgrades.MOD_ID)
public final class BotaniaBackpackUpgrades {
	public static final String MOD_ID = "botaniabackpack";

	public BotaniaBackpackUpgrades(IEventBus modBus, ModContainer modContainer) {
		ModDataComponents.register(modBus);
		ModAttachments.register(modBus);
		ModItems.register(modBus);
		ModCreativeTabs.register(modBus);
		ModFluidTypes.register(modBus);
		ModFluids.register(modBus);
		modBus.addListener(this::commonSetup);
		modContainer.registerConfig(ModConfig.Type.SERVER, BotaniaBackpackConfig.SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			UpgradeContainerRegistry.register(ModItems.MANA_TANK_UPGRADE.getId(), ManaTankUpgradeContainer.TYPE);
			UpgradeContainerRegistry.register(ModItems.MANA_INFUSION_UPGRADE.getId(), ManaInfusionUpgradeContainer.BASIC_TYPE);
			UpgradeContainerRegistry.register(ModItems.ADVANCED_MANA_INFUSION_UPGRADE.getId(), ManaInfusionUpgradeContainer.ADVANCED_TYPE);
		});
	}
}
