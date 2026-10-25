package velrondevs.botaniabackpack.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.IntSupplier;

public final class BotaniaBackpackConfig {
	private static final int DEFAULT_MANA_CAPACITY = 1_000_000;

	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec.IntValue MANA_CAPACITY = BUILDER
			.comment("Maximum mana stored by one mana tank upgrade.")
			.defineInRange("manaTank.capacity", DEFAULT_MANA_CAPACITY, 1, Integer.MAX_VALUE);
	public static final ModConfigSpec.IntValue MANA_INFUSION_FILTER_SLOTS = BUILDER
			.comment("Number of filter slots in the Mana Infusion Upgrade.")
			.defineInRange("manaInfusion.filterSlots", 9, 1, 20);
	public static final ModConfigSpec.IntValue MANA_INFUSION_SLOTS_IN_ROW = BUILDER
			.comment("Number of Mana Infusion Upgrade filter slots displayed in one row.")
			.defineInRange("manaInfusion.slotsInRow", 3, 1, 6);
	public static final ModConfigSpec.IntValue ADVANCED_MANA_INFUSION_FILTER_SLOTS = BUILDER
			.comment("Number of filter slots in the Advanced Mana Infusion Upgrade.")
			.defineInRange("advancedManaInfusion.filterSlots", 16, 1, 20);
	public static final ModConfigSpec.IntValue ADVANCED_MANA_INFUSION_SLOTS_IN_ROW = BUILDER
			.comment("Number of Advanced Mana Infusion Upgrade filter slots displayed in one row.")
			.defineInRange("advancedManaInfusion.slotsInRow", 4, 1, 6);
	public static final ModConfigSpec SPEC = BUILDER.build();

	/** Reads a config value, falling back to its default while the server config is not loaded yet (e.g. client side before sync). */
	public static IntSupplier safe(ModConfigSpec.IntValue value) {
		return () -> {
			try {
				return value.get();
			} catch (IllegalStateException exception) {
				return value.getDefault();
			}
		};
	}

	private BotaniaBackpackConfig() {}
}
