package velrondevs.botaniabackpack.client.hud;

import velrondevs.botaniabackpack.compat.botania.ManaBackpackPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import velrondevs.botania.api.BotaniaAPIClient;
import velrondevs.botania.api.block.WandHUD;

public final class ManaWandHud implements WandHUD {
	private static final int MANA_COLOR = 0x20BFFF;
	private final BackpackBlockEntity backpack;

	public ManaWandHud(BackpackBlockEntity backpack) {
		this.backpack = backpack;
	}

	@Override
	public void renderHUD(GuiGraphics gui, Minecraft minecraft) {
		ManaBackpackPool.findUpgrade(backpack)
				.ifPresent(upgrade -> BotaniaAPIClient.instance().drawSimpleManaHUD(gui, MANA_COLOR, upgrade.getMana(), upgrade.getCapacity(),
						Component.translatable("item.botaniabackpack.mana_tank_upgrade").getString()));
	}
}
