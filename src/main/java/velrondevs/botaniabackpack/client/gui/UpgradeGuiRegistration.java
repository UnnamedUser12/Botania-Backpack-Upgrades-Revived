package velrondevs.botaniabackpack.client.gui;

import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;

/** Hooks the upgrade settings tabs and the tank bar into Sophisticated Core's GUI. Kept separate so it can be verified without a client. */
public final class UpgradeGuiRegistration {
	public static void register() {
		UpgradeGuiManager.registerTab(ManaTankUpgradeContainer.TYPE, ManaTankUpgradeTab::new);
		UpgradeGuiManager.registerInventoryControl(ManaTankUpgradeContainer.TYPE, ManaTankInventoryControl::new);
		UpgradeGuiManager.registerTab(ManaInfusionUpgradeContainer.BASIC_TYPE, ManaInfusionUpgradeTab.Basic::new);
		UpgradeGuiManager.registerTab(ManaInfusionUpgradeContainer.ADVANCED_TYPE, ManaInfusionUpgradeTab.Advanced::new);
	}

	private UpgradeGuiRegistration() {}
}
