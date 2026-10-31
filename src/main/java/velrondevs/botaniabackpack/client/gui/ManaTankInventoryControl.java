package velrondevs.botaniabackpack.client.gui;

import velrondevs.botaniabackpack.registry.ModFluids;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeInventoryControlBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;
import velrondevs.botania.api.BotaniaCapabilities;

import java.util.List;
import java.util.Optional;

public final class ManaTankInventoryControl extends UpgradeInventoryControlBase {
	private static final TextureBlitData OVERLAY = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(47, 30),
			new Dimension(16, 18));

	private final ManaTankUpgradeContainer container;
	private final Position position;
	private final int height;
	private final StorageScreenBase<?> screen;

	public ManaTankInventoryControl(int upgradeSlot, ManaTankUpgradeContainer container, Position position, int height,
			StorageScreenBase<?> screen) {
		this.container = container;
		this.position = position;
		this.height = height;
		this.screen = screen;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		renderTankBackground(guiGraphics);
		renderMana(guiGraphics);
		renderTankOverlay(guiGraphics);
	}

	private void renderTankBackground(GuiGraphics guiGraphics) {
		GuiHelper.blit(guiGraphics, getTankLeft(), position.y(), GuiHelper.BAR_BACKGROUND_TOP, 18, height < 36 ? height / 2 : 18);
		int yOffset = 18;
		for (int i = 0; i < (height - 36) / 18; i++) {
			GuiHelper.blit(guiGraphics, getTankLeft(), position.y() + yOffset, GuiHelper.BAR_BACKGROUND_MIDDLE);
			yOffset += 18;
		}
		GuiHelper.blit(guiGraphics, getTankLeft(), position.y() + (height < 36 ? height / 2 : yOffset), GuiHelper.BAR_BACKGROUND_BOTTOM, 18,
				height < 36 ? height / 2 : 18);
	}

	private void renderTankOverlay(GuiGraphics guiGraphics) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100);
		for (int i = 0, yOffset = 0; i < height / 18; i++, yOffset += 18) {
			GuiHelper.blit(guiGraphics, getTankLeft() + 1, position.y() + yOffset, OVERLAY);
		}
		guiGraphics.pose().popPose();
	}

	private void renderMana(GuiGraphics guiGraphics) {
		int mana = container.getMana();
		if (mana <= 0) {
			return;
		}
		int displayHeight = Math.max(1, Math.round((height - 2) * ((float) mana / container.getCapacity())));
		FluidStack displayFluid = new FluidStack(ModFluids.MANA_DISPLAY.get(), 1);
		IClientFluidTypeExtensions properties = IClientFluidTypeExtensions.of(displayFluid.getFluid());
		TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(properties.getStillTexture(displayFluid));
		int fluidY = position.y() + height - 1 - displayHeight;
		GuiHelper.renderTiledFluidTextureAtlas(guiGraphics, sprite, properties.getTintColor(displayFluid), position.x() + 10, fluidY,
				displayHeight);
	}

	private int getTankLeft() {
		return position.x() + 9;
	}

	@Override
	public boolean handleMouseReleased(double mouseX, double mouseY, int button) {
		if (button != 1 || !isMouseOverTank(mouseX, mouseY)) {
			return false;
		}

		var cursorStack = screen.getMenu().getCarried();
		if (cursorStack.getCount() != 1 || cursorStack.getCapability(BotaniaCapabilities.MANA_ITEM) == null) {
			return false;
		}

		container.requestCursorInteraction();
		return true;
	}

	@Override
	public void renderErrorOverlay(GuiGraphics guiGraphics) {
		screen.renderOverlay(guiGraphics, StorageScreenBase.ERROR_SLOT_COLOR, getTankLeft() + 1, position.y() + 1, 16, height - 2);
	}

	@Override
	public void renderTooltip(StorageScreenBase<?> ignored, GuiGraphics guiGraphics, int mouseX, int mouseY) {
		int screenX = screen.getGuiLeft() + position.x() + 10;
		int screenY = screen.getGuiTop() + position.y() + 1;
		if (mouseX >= screenX && mouseX < screenX + 16 && mouseY >= screenY && mouseY < screenY + height - 2) {
			Component contents = Component.translatable("gui.botaniabackpack.mana_tank.contents", String.format("%,d", container.getMana()),
					String.format("%,d", container.getCapacity()));
			Component interaction = Component.translatable("gui.botaniabackpack.mana_tank.interaction");
			guiGraphics.renderTooltip(screen.getMinecraft().font, List.of(contents, interaction), Optional.empty(), mouseX, mouseY);
		}
	}

	private boolean isMouseOverTank(double mouseX, double mouseY) {
		int screenX = screen.getGuiLeft() + getTankLeft();
		int screenY = screen.getGuiTop() + position.y();
		return mouseX >= screenX && mouseX < screenX + 18 && mouseY >= screenY && mouseY < screenY + height;
	}
}
