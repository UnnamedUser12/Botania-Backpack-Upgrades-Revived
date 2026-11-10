package velrondevs.botaniabackpack.datagen;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.datagen.providers.lang.EnUsLanguageProvider;
import velrondevs.botaniabackpack.datagen.providers.lang.ZhCnLanguageProvider;
import velrondevs.botaniabackpack.datagen.providers.models.UpgradeItemModelProvider;
import velrondevs.botaniabackpack.datagen.providers.recipes.UpgradeRecipeProvider;
import velrondevs.botaniabackpack.datagen.providers.tags.UpgradeItemTagProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
public final class BotaniaBackpackDatagen {
	@SubscribeEvent
	public static void gatherServerData(GatherDataEvent.Server event) {
		PackOutput output = event.getGenerator().getPackOutput();

		event.addProvider(new UpgradeRecipeProvider.Runner(output, event.getLookupProvider()));
		event.addProvider(new UpgradeItemTagProvider(output, event.getLookupProvider()));
	}

	@SubscribeEvent
	public static void gatherClientData(GatherDataEvent.Client event) {
		PackOutput output = event.getGenerator().getPackOutput();

		event.addProvider(new UpgradeItemModelProvider(output));
		event.addProvider(new EnUsLanguageProvider(output));
		event.addProvider(new ZhCnLanguageProvider(output));
	}

	private BotaniaBackpackDatagen() {}
}
