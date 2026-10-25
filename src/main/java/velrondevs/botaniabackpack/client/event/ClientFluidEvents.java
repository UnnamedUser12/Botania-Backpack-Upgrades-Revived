package velrondevs.botaniabackpack.client.event;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModFluidTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client look of the display-only mana fluid (tinted water). */
@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID, value = Dist.CLIENT)
public final class ClientFluidEvents {
	private static final ResourceLocation STILL_TEXTURE = ResourceLocation.withDefaultNamespace("block/water_still");
	private static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.withDefaultNamespace("block/water_flow");
	private static final int MANA_TINT = 0xFF20BFFF;

	@SubscribeEvent
	public static void registerFluidExtensions(RegisterClientExtensionsEvent event) {
		event.registerFluidType(new IClientFluidTypeExtensions() {
			@Override
			public ResourceLocation getStillTexture() {
				return STILL_TEXTURE;
			}

			@Override
			public ResourceLocation getFlowingTexture() {
				return FLOWING_TEXTURE;
			}

			@Override
			public int getTintColor() {
				return MANA_TINT;
			}
		}, ModFluidTypes.MANA_DISPLAY.get());
	}

	private ClientFluidEvents() {}
}
