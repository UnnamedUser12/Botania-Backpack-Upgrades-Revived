package velrondevs.botaniabackpack.client.gui;

import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeContainer;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicControl;

import static net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicControlBase.MatchButton.ALLOW_LIST;
import static net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicControlBase.MatchButton.PRIMARY_MATCH;

public abstract class ManaInfusionUpgradeTab extends UpgradeSettingsTab<ManaInfusionUpgradeContainer> {
	private FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> filterLogicControl;

	protected ManaInfusionUpgradeTab(ManaInfusionUpgradeContainer container, Position position, StorageScreenBase<?> screen,
			Component tabLabel, Component closedTooltip) {
		super(container, position, screen, tabLabel, closedTooltip);
		addHideableChild(new ToggleButton<>(new Position(x + 3, y + 24), ButtonDefinitions.WORK_IN_GUI,
				button -> getContainer().setShouldWorkInGUI(!getContainer().shouldWorkInGUI()), getContainer()::shouldWorkInGUI));
	}

	protected final void setFilterLogicControl(FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> filterLogicControl) {
		this.filterLogicControl = addHideableChild(filterLogicControl);
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}

	public static final class Basic extends ManaInfusionUpgradeTab {
		public Basic(ManaInfusionUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
			super(container, position, screen, Component.translatable("gui.botaniabackpack.upgrade.mana_infusion"),
					Component.translatable("gui.botaniabackpack.upgrade.mana_infusion.tooltip"));
			setFilterLogicControl(new FilterLogicControl.Basic(screen, new Position(x + 3, y + 44),
					getContainer().getFilterLogicContainer(), getContainer().getUpgradeWrapper().getSlotsInRow()));
		}
	}

	public static final class Advanced extends ManaInfusionUpgradeTab {
		public Advanced(ManaInfusionUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
			super(container, position, screen, Component.translatable("gui.botaniabackpack.upgrade.advanced_mana_infusion"),
					Component.translatable("gui.botaniabackpack.upgrade.advanced_mana_infusion.tooltip"));
			setFilterLogicControl(new FilterLogicControl<>(screen, new Position(x + 3, y + 44),
					getContainer().getFilterLogicContainer(), getContainer().getUpgradeWrapper().getSlotsInRow(), ALLOW_LIST, PRIMARY_MATCH));
		}
	}
}
