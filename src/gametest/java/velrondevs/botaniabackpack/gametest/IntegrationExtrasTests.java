package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import top.theillusivec4.curios.api.CuriosApi;
import velrondevs.botania.api.mana.ManaItemHandler;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModCreativeTabs;
import velrondevs.botaniabackpack.registry.ModItems;
import velrondevs.botaniabackpack.upgrade.ManaInfusionUpgradeContainer;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeContainer;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class IntegrationExtrasTests {
	private static final String TEMPLATE = "empty";

	@GameTest(template = TEMPLATE)
	public static void upgradesOfTheSameKindConflict(GameTestHelper helper) {
		ItemStack backpack = TestSupport.newBackpack();
		var wrapper = TestSupport.wrapper(backpack);
		for (var upgrade : new net.neoforged.neoforge.registries.DeferredHolder[] {ModItems.MANA_TANK_UPGRADE, ModItems.MANA_SUPPLY_UPGRADE,
				ModItems.MANA_INFUSION_UPGRADE}) {
			var item = (net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem<?>) upgrade.get();
			ItemStack stack = new ItemStack((net.minecraft.world.level.ItemLike) upgrade.get());
			helper.assertTrue(item.canAddUpgradeTo(wrapper, stack, true, false).successful(), upgrade.getId() + " installs in an empty backpack");
			TestSupport.install(wrapper, (net.minecraft.world.item.Item) upgrade.get());
			helper.assertTrue(!item.canAddUpgradeTo(wrapper, stack, true, false).successful(), "a second " + upgrade.getId() + " must be refused");
		}
		// Basic and advanced infusion are mutually exclusive
		var advanced = ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get();
		helper.assertTrue(!advanced.canAddUpgradeTo(wrapper, new ItemStack(advanced), true, false).successful(),
				"advanced infusion cannot join an installed basic infusion upgrade");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void settingsContainersAreRegisteredForTheConfigurableUpgrades(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = TestSupport.backpackWithTank(0);
		var wrapper = TestSupport.wrapper(backpack);
		TestSupport.install(wrapper, ModItems.MANA_INFUSION_UPGRADE.get());
		TestSupport.install(wrapper, ModItems.MANA_SUPPLY_UPGRADE.get());

		var tankContainer = UpgradeContainerRegistry.instantiateContainer(player, 1, TestSupport.tank(wrapper));
		helper.assertTrue(tankContainer.isPresent() && tankContainer.get() instanceof ManaTankUpgradeContainer, "tank container registered");
		var infusionContainer = UpgradeContainerRegistry.instantiateContainer(player, 2, TestSupport.infusion(wrapper));
		helper.assertTrue(infusionContainer.isPresent() && infusionContainer.get() instanceof ManaInfusionUpgradeContainer, "infusion container registered");
		var supplyWrapper = wrapper.getUpgradeHandler().getTypeWrappers(velrondevs.botaniabackpack.upgrade.ManaSupplyUpgradeItem.TYPE).get(0);
		helper.assertTrue(UpgradeContainerRegistry.instantiateContainer(player, 3, supplyWrapper).isEmpty(), "supply has no settings tab");

		// Advanced infusion shares the same container type
		ItemStack advanced = TestSupport.newBackpack();
		var advancedWrapper = TestSupport.wrapper(advanced);
		TestSupport.install(advancedWrapper, ModItems.ADVANCED_MANA_INFUSION_UPGRADE.get());
		helper.assertTrue(UpgradeContainerRegistry.instantiateContainer(player, 4, TestSupport.infusion(advancedWrapper)).isPresent(),
				"advanced infusion container registered");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void tankIsRenderedInThePlacedBackpackModelAndSurvivesTheRenderDataRoundTrip(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(0);
		var wrapper = TestSupport.wrapper(backpack);
		helper.assertTrue(wrapper.getRenderInfo().getTankRenderInfos().size() <= 1, "baseline render info");
		TestSupport.tank(wrapper).receiveInternalMana(500_000);

		var tanks = wrapper.getRenderInfo().getTankRenderInfos();
		helper.assertTrue(tanks.size() == 1, "a half-full mana tank must be rendered, tank render infos: " + tanks.size());
		var info = tanks.values().iterator().next();
		helper.assertTrue(info.getFluid().isPresent() && info.getFluid().get().getFluid() == velrondevs.botaniabackpack.registry.ModFluids.MANA_DISPLAY.get(),
				"the display fluid is the mana pseudo fluid");
		helper.assertTrue(Math.abs(info.getFillRatio() - 0.5F) < 0.001F, "fill ratio 0.5 but was " + info.getFillRatio());

		var roundTrip = net.p3pp3rf1y.sophisticatedcore.upgrades.IRenderedTankUpgrade.TankRenderInfo.deserialize(info.serialize());
		helper.assertTrue(roundTrip.getFluid().isPresent() && roundTrip.getFillRatio() == info.getFillRatio(), "render info survives serialization");

		TestSupport.tank(wrapper).extractInternalMana(500_000);
		helper.assertTrue(wrapper.getRenderInfo().getTankRenderInfos().values().stream().allMatch(i -> i.getFluid().isEmpty()),
				"an emptied tank renders no fluid");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void backpackWornInTheCuriosBackSlotSuppliesManaOnlyOnce(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		ItemStack backpack = TestSupport.backpackWithTank(10_000);
		TestSupport.install(TestSupport.wrapper(backpack), ModItems.MANA_SUPPLY_UPGRADE.get());
		var curios = CuriosApi.getCuriosInventory(player);
		helper.assertTrue(curios.isPresent(), "player has a Curios inventory");
		curios.get().setEquippedCurio("back", 0, backpack);

		ItemStack consumer = new ItemStack(Items.DIAMOND_SWORD);
		var handler = ManaItemHandler.instance();
		helper.assertTrue(!handler.requestManaExact(consumer, player, 12_000, true), "a single 10,000 tank cannot give 12,000 (no double counting)");
		helper.assertTrue(handler.requestMana(consumer, player, 6_000, true) == 6_000, "first 6000");
		helper.assertTrue(handler.requestMana(consumer, player, 6_000, true) == 4_000, "only 4000 remain: Curios + backpack handler must not both count the tank");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void creativeTabsListTheUpgrades(GameTestHelper helper) {
		var access = helper.getLevel().registryAccess();
		CreativeModeTabs.tryRebuildTabContents(FeatureFlags.REGISTRY.allFlags(), true, access);

		var own = ModCreativeTabs.MAIN.get().getDisplayItems();
		var sophisticated = net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems.CREATIVE_TAB.get().getDisplayItems();
		for (var upgrade : java.util.List.of(ModItems.MANA_TANK_UPGRADE, ModItems.MANA_SUPPLY_UPGRADE, ModItems.MANA_INFUSION_UPGRADE,
				ModItems.ADVANCED_MANA_INFUSION_UPGRADE)) {
			helper.assertTrue(own.stream().anyMatch(s -> s.is(upgrade.get())), upgrade.getId() + " is in the mod's own creative tab");
			helper.assertTrue(sophisticated.stream().anyMatch(s -> s.is(upgrade.get())), upgrade.getId() + " is in the Sophisticated Backpacks tab");
		}
		helper.assertTrue(own.size() == 4, "own tab has exactly the 4 upgrades, has " + own.size());
		helper.succeed();
	}

	@SuppressWarnings("unused")
	private static void unused(RenderInfo r, BackpackItem b, net.minecraft.resources.ResourceKey<?> k) {
		var x = Registries.ITEM;
	}
}
