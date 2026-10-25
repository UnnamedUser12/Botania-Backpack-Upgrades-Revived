package velrondevs.botaniabackpack.registry;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * A non-placeable fluid used only by the Sophisticated Backpacks tank renderer.
 * Mana remains an integer Botania resource and is never exposed as a fluid.
 */
public final class ModFluids {
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, BotaniaBackpackUpgrades.MOD_ID);

	public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> MANA_DISPLAY = FLUIDS.register("mana_display",
			() -> new BaseFlowingFluid.Source(properties()));
	public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> FLOWING_MANA_DISPLAY = FLUIDS.register("flowing_mana_display",
			() -> new BaseFlowingFluid.Flowing(properties()));

	private static BaseFlowingFluid.Properties properties() {
		return new BaseFlowingFluid.Properties(ModFluidTypes.MANA_DISPLAY, MANA_DISPLAY, FLOWING_MANA_DISPLAY);
	}

	public static void register(IEventBus modBus) {
		FLUIDS.register(modBus);
	}

	private ModFluids() {}
}
