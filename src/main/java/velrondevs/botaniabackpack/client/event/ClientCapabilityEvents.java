package velrondevs.botaniabackpack.client.event;

import velrondevs.botaniabackpack.client.hud.ManaWandHud;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.compat.botania.ManaBackpackPool;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.p3pp3rf1y.sophisticatedbackpacks.init.ModBlocks;
import velrondevs.botania.api.BotaniaClientCapabilities;

/** Shows the exact mana amount in the Wand of the Forest HUD when pointing at a placed backpack with a mana tank. */
@EventBusSubscriber(modid = BotaniaBackpackUpgrades.MOD_ID, value = Dist.CLIENT)
public final class ClientCapabilityEvents {
	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, ModBlocks.BACKPACK_TILE_TYPE.get(),
				(backpack, context) -> ManaBackpackPool.findUpgrade(backpack).isPresent() ? new ManaWandHud(backpack) : null);
	}

	private ClientCapabilityEvents() {}
}
