package velrondevs.botaniabackpack.datagen;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.datagen.providers.lang.EnUsLanguageProvider;
import velrondevs.botaniabackpack.datagen.providers.lang.ZhCnLanguageProvider;
import velrondevs.botaniabackpack.datagen.providers.models.UpgradeItemModelProvider;
import velrondevs.botaniabackpack.datagen.providers.recipes.UpgradeRecipeProvider;
import velrondevs.botaniabackpack.datagen.providers.tags.UpgradeItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

/** Entry point of {@code ./gradlew runData}. */
@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID)
public final class BotaniaBackpackDatagen {
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		PackOutput output = generator.getPackOutput();
		ExistingFileHelper fileHelper = event.getExistingFileHelper();
		CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
		boolean server = event.includeServer();
		boolean client = event.includeClient();

		generator.addProvider(server, new UpgradeRecipeProvider(output, lookup));
		generator.addProvider(server, new UpgradeItemTagProvider(output, lookup, fileHelper));

		generator.addProvider(client, new UpgradeItemModelProvider(output, fileHelper));
		generator.addProvider(client, new EnUsLanguageProvider(output));
		generator.addProvider(client, new ZhCnLanguageProvider(output));
	}

	private BotaniaBackpackDatagen() {}
}
