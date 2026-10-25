package velrondevs.botaniabackpack.registry;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Upgrade state formerly kept in raw item NBT, now stored as data components on the upgrade stack.
 */
public final class ModDataComponents {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE,
			BotaniaBackpackUpgrades.MOD_ID);

	public static final Supplier<DataComponentType<Integer>> MANA = DATA_COMPONENTS.registerComponentType("mana",
			builder -> builder.persistent(Codec.intRange(0, Integer.MAX_VALUE)).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final Supplier<DataComponentType<ManaTransferMode>> TRANSFER_MODE = DATA_COMPONENTS.registerComponentType("transfer_mode",
			builder -> builder.persistent(ManaTransferMode.CODEC).networkSynchronized(ManaTransferMode.STREAM_CODEC));

	public static void register(IEventBus modBus) {
		DATA_COMPONENTS.register(modBus);
	}

	private ModDataComponents() {}
}
