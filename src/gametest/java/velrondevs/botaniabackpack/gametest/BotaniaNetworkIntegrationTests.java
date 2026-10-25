package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import velrondevs.botania.api.BotaniaCapabilities;
import velrondevs.botania.api.mana.spark.SparkAttachable;
import velrondevs.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import velrondevs.botania.common.entity.ManaSparkEntity;
import velrondevs.botania.registry.BotaniaEntities;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.upgrade.ManaTankUpgradeWrapper;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;

/** Tests against Botania's own machines (spreader, burst, spark) to prove the backpack really behaves as a mana pool. */
@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BotaniaNetworkIntegrationTests {
	// Spark tests run in their own batches: sparks of every test in range share one network and would exchange mana with each other
	private static final String TEMPLATE = "empty";

	private static Block botaniaBlock(String path) {
		return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("botania", path));
	}

	private static ManaSpreaderBlockEntity placeSpreader(GameTestHelper helper, BlockPos rel) {
		helper.setBlock(rel, botaniaBlock("mana_spreader"));
		return (ManaSpreaderBlockEntity) helper.getBlockEntity(rel);
	}

	@GameTest(template = TEMPLATE)
	public static void adjacentSpreaderDrawsManaFromThePlacedBackpack(GameTestHelper helper) {
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, new BlockPos(2, 1, 1), TestSupport.backpackWithTank(5_000));
		ManaSpreaderBlockEntity spreader = placeSpreader(helper, new BlockPos(1, 1, 1));
		ManaTankUpgradeWrapper tank = TestSupport.tank(be.getBackpackWrapper());

		helper.runAfterDelay(5, () -> {
			int taken = 5_000 - tank.getMana();
			helper.assertTrue(taken > 0, "the spreader must pull mana out of the adjacent backpack tank");
			helper.assertTrue(spreader.getCurrentMana() == taken, "mana conserved: spreader has " + spreader.getCurrentMana() + " tank lost " + taken);
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void spreaderDoesNotDrawFromAnInputOnlyBackpack(GameTestHelper helper) {
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, new BlockPos(2, 1, 1), TestSupport.backpackWithTank(5_000));
		TestSupport.tank(be.getBackpackWrapper()).setTransferMode(ManaTransferMode.INPUT);
		ManaSpreaderBlockEntity spreader = placeSpreader(helper, new BlockPos(1, 1, 1));

		helper.runAfterDelay(5, () -> {
			helper.assertTrue(spreader.getCurrentMana() == 0, "INPUT-only tank must not feed the spreader");
			helper.assertTrue(TestSupport.tank(be.getBackpackWrapper()).getMana() == 5_000, "tank untouched");
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE)
	public static void spreaderBurstTravelsToThePlacedBackpackAndFillsTheTank(GameTestHelper helper) {
		BlockPos spreaderPos = new BlockPos(0, 1, 1);
		BlockPos backpackPos = new BlockPos(2, 1, 1);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, backpackPos, TestSupport.backpackWithTank(0));
		ManaSpreaderBlockEntity spreader = placeSpreader(helper, spreaderPos);

		// Find the rotation that aims the beam at the backpack: its beam scan must detect the pool as a receiver
		boolean aimed = false;
		for (float rotation : new float[] {0F, 90F, 180F, 270F}) {
			spreader.setRotationX(rotation);
			spreader.setRotationY(0F);
			spreader.checkForReceiver();
			if (helper.absolutePos(backpackPos).equals(spreader.getBinding())) {
				aimed = true;
				break;
			}
		}
		helper.assertTrue(aimed, "the spreader beam must see the backpack as a mana receiver (binding=" + spreader.getBinding() + ")");

		spreader.receiveMana(spreader.getMaxMana());
		helper.runAfterDelay(40, () -> {
			int mana = TestSupport.tank(be.getBackpackWrapper()).getMana();
			helper.assertTrue(mana > 0, "a burst must have delivered mana into the backpack tank, tank=" + mana);
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE, batch = "sparkAttach")
	public static void sparkOnPlacedBackpackIsRecognisedAsAttached(GameTestHelper helper) {
		BlockPos backpackPos = new BlockPos(1, 1, 1);
		TestSupport.placeBackpack(helper, backpackPos, TestSupport.backpackWithTank(1_000));
		ManaSparkEntity spark = new ManaSparkEntity(helper.getLevel());
		var above = helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5, 2.0, 1.5));
		spark.setPos(above);
		helper.getLevel().addFreshEntity(spark);

		helper.runAfterDelay(3, () -> {
			SparkAttachable attachable = spark.getAttachedTile();
			helper.assertTrue(attachable != null, "spark above the backpack must find the SparkAttachable below it");
			helper.assertTrue(attachable.getAttachedSpark() == spark, "the backpack reports the spark above it");
			helper.assertTrue(spark.isAlive(), "spark stays alive on the backpack tank");
			helper.succeed();
		});
	}

	@GameTest(template = TEMPLATE, batch = "sparkNetwork")
	public static void sparksMoveManaBetweenTwoBackpackTanksWithoutLoss(GameTestHelper helper) {
		BlockPos fullPos = new BlockPos(0, 1, 1);
		BlockPos emptyPos = new BlockPos(2, 1, 1);
		BackpackBlockEntity full = TestSupport.placeBackpack(helper, fullPos, TestSupport.backpackWithTank(20_000));
		BackpackBlockEntity empty = TestSupport.placeBackpack(helper, emptyPos, TestSupport.backpackWithTank(0));
		for (BlockPos pos : new BlockPos[] {fullPos, emptyPos}) {
			boolean receiving = pos.equals(emptyPos);
			ManaSparkEntity spark = new ManaSparkEntity(helper.getLevel());
			spark.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5)));
			if (receiving) {
				spark.setUpgrade(velrondevs.botania.api.mana.spark.SparkUpgradeType.DOMINANT); // dominant sparks pull from plain sparks on pools
			}
			helper.getLevel().addFreshEntity(spark);
		}

		helper.runAfterDelay(60, () -> {
			int fullMana = TestSupport.tank(full.getBackpackWrapper()).getMana();
			int emptyMana = TestSupport.tank(empty.getBackpackWrapper()).getMana();
			helper.assertTrue(emptyMana > 0, "spark network must have moved mana into the empty tank (empty=" + emptyMana + ", full=" + fullMana + ")");
			helper.assertTrue(fullMana + emptyMana == 20_000, "total mana is conserved: " + (fullMana + emptyMana));
			helper.succeed();
		});
	}

	@SuppressWarnings("unused")
	private static void unusedImports() {
		ItemStack.EMPTY.isEmpty();
		var ignored = BotaniaCapabilities.MANA_ITEM;
		var ignoredEntity = BotaniaEntities.SPARK;
	}
}
