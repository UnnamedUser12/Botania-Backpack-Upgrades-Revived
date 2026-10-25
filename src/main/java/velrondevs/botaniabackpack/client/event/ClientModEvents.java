package velrondevs.botaniabackpack.client.event;

import velrondevs.botaniabackpack.client.gui.ManaTankUpgradeTab;
import velrondevs.botaniabackpack.client.gui.ManaTankInventoryPart;
import velrondevs.botaniabackpack.client.gui.ManaInfusionUpgradeTab;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {
	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			UpgradeGuiManager.registerTab(ManaTankUpgradeContainer.TYPE, ManaTankUpgradeTab::new);
			UpgradeGuiManager.registerInventoryPart(ManaTankUpgradeContainer.TYPE, ManaTankInventoryPart::new);
			UpgradeGuiManager.registerTab(ManaInfusionUpgradeContainer.BASIC_TYPE, ManaInfusionUpgradeTab.Basic::new);
			UpgradeGuiManager.registerTab(ManaInfusionUpgradeContainer.ADVANCED_TYPE, ManaInfusionUpgradeTab.Advanced::new);
		});
	}

	private ClientModEvents() {}
}
