package velrondevs.botaniabackpack.registry;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluidTypes {
	private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES,
			BotaniaBackpackUpgrades.MOD_ID);

	/** Display-only fluid type used by the Sophisticated Backpacks tank renderer. */
	public static final DeferredHolder<FluidType, FluidType> MANA_DISPLAY = FLUID_TYPES.register("mana_display",
			() -> new FluidType(FluidType.Properties.create().lightLevel(8)));

	public static void register(IEventBus modBus) {
		FLUID_TYPES.register(modBus);
	}

	private ModFluidTypes() {}
}
