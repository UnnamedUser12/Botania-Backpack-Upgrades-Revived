package velrondevs.botaniabackpack.registry;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.compat.botania.ManaBackpackPool;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;

import java.util.function.Supplier;

public final class ModAttachments {
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,
			BotaniaBackpackUpgrades.MOD_ID);

	/** Non-persistent per-block-entity Botania pool adapter; lives and dies with the placed backpack. */
	public static final Supplier<AttachmentType<ManaBackpackPool>> MANA_POOL = ATTACHMENT_TYPES.register("mana_pool",
			() -> AttachmentType.<ManaBackpackPool>builder(holder -> new ManaBackpackPool((BackpackBlockEntity) holder)).build());

	public static void register(IEventBus modBus) {
		ATTACHMENT_TYPES.register(modBus);
	}

	private ModAttachments() {}
}
