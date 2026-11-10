package velrondevs.botaniabackpack.client.event;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID, value = Dist.CLIENT)
public final class ClientFluidEvents {
	private static final Identifier STILL_TEXTURE = Identifier.withDefaultNamespace("block/water_still");
	private static final Identifier FLOWING_TEXTURE = Identifier.withDefaultNamespace("block/water_flow");
	private static final int MANA_TINT = 0xFF20BFFF;

	@SubscribeEvent
	public static void registerFluidModels(RegisterFluidModelsEvent event) {
		event.register(new FluidModel.Unbaked(new Material(STILL_TEXTURE), new Material(FLOWING_TEXTURE), null,
				FluidTintSources.constant(MANA_TINT)), ModFluids.MANA_DISPLAY.get(), ModFluids.FLOWING_MANA_DISPLAY.get());
	}

	private ClientFluidEvents() {}
}
