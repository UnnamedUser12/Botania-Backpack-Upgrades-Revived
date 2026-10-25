package velrondevs.botaniabackpack.client.gui;

import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;

import java.util.Map;

import static net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions.createToggleButtonDefinition;

public final class ManaTankUpgradeTab extends UpgradeSettingsTab<ManaTankUpgradeContainer> {
	private static final ButtonDefinition.Toggle<ManaTransferMode> TRANSFER_MODE = createToggleButtonDefinition(Map.of(
			ManaTransferMode.BOTH,
			GuiHelper.getButtonStateData(new UV(0, 32), Dimension.SQUARE_16, new Position(1, 1),
					Component.translatable("gui.botaniabackpack.transfer_mode.both")),
			ManaTransferMode.INPUT,
			GuiHelper.getButtonStateData(new UV(16, 32), Dimension.SQUARE_16, new Position(1, 1),
					Component.translatable("gui.botaniabackpack.transfer_mode.input")),
			ManaTransferMode.OUTPUT,
			GuiHelper.getButtonStateData(new UV(32, 32), Dimension.SQUARE_16, new Position(1, 1),
					Component.translatable("gui.botaniabackpack.transfer_mode.output"))));

	public ManaTankUpgradeTab(ManaTankUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
		super(container, position, screen, Component.translatable("gui.botaniabackpack.upgrade.mana_tank"),
				Component.translatable("gui.botaniabackpack.upgrade.mana_tank.tooltip"));
		openTabDimension = new Dimension(48, 48);
		addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), TRANSFER_MODE,
				button -> getContainer().setTransferMode(getContainer().getTransferMode().next()), getContainer()::getTransferMode));
	}

	@Override
	protected void moveSlotsToTab() {
		// The upgrade has no item slots.
	}
}
