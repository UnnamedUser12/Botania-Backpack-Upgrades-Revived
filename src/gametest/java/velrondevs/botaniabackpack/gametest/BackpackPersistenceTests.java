package velrondevs.botaniabackpack.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.upgrade.ManaTransferMode;

import java.util.List;

@GameTestHolder(BotaniaBackpackUpgrades.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BackpackPersistenceTests {
	private static final String TEMPLATE = "empty";

	@GameTest(template = TEMPLATE)
	public static void manaSurvivesPlacingTheBackpackWithTheItem(GameTestHelper helper) {
		ServerPlayer player = TestSupport.mockPlayer(helper);
		BlockPos floor = new BlockPos(1, 1, 1);
		helper.setBlock(floor, Blocks.STONE);
		ItemStack backpack = TestSupport.backpackWithTank(54_321);
		TestSupport.tank(TestSupport.wrapper(backpack)).setTransferMode(ManaTransferMode.INPUT);
		player.setItemInHand(InteractionHand.MAIN_HAND, backpack);
		player.setShiftKeyDown(true); // backpacks only place while sneaking

		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(floor)).add(0, 0.5, 0), Direction.UP, helper.absolutePos(floor), false);
		InteractionResult result = backpack.useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, backpack, hit));
		helper.assertTrue(result.consumesAction(), "backpack placed: " + result);

		BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(floor.above()));
		helper.assertTrue(be instanceof BackpackBlockEntity, "backpack block entity exists above the stone");
		var tank = TestSupport.tank(((BackpackBlockEntity) be).getBackpackWrapper());
		helper.assertTrue(tank.getMana() == 54_321, "placed tank mana " + tank.getMana());
		helper.assertTrue(tank.getTransferMode() == ManaTransferMode.INPUT, "placed tank transfer mode");
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void manaSurvivesBlockEntitySaveAndLoad(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		BackpackBlockEntity be = TestSupport.placeBackpack(helper, rel, TestSupport.backpackWithTank(777));
		var registries = helper.getLevel().registryAccess();
		var tag = be.saveWithFullMetadata(registries);
		BlockEntity loaded = BlockEntity.loadStatic(helper.absolutePos(rel), be.getBlockState(), tag, registries);
		helper.assertTrue(loaded instanceof BackpackBlockEntity, "reloaded as backpack block entity");
		var tank = TestSupport.tank(((BackpackBlockEntity) loaded).getBackpackWrapper());
		helper.assertTrue(tank.getMana() == 777, "reloaded mana " + tank.getMana());
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void manaSurvivesBreakingThePlacedBackpack(GameTestHelper helper) {
		BlockPos rel = new BlockPos(1, 1, 1);
		TestSupport.placeBackpack(helper, rel, TestSupport.backpackWithTank(31_337));
		helper.getLevel().destroyBlock(helper.absolutePos(rel), true);

		List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
				new net.minecraft.world.phys.AABB(helper.absolutePos(rel)).inflate(3));
		ItemStack dropped = drops.stream().map(ItemEntity::getItem).filter(s -> s.getItem() instanceof net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem)
				.findFirst().orElse(ItemStack.EMPTY);
		helper.assertTrue(!dropped.isEmpty(), "the backpack item should drop when broken");
		helper.assertTrue(TestSupport.tank(BackpackWrapper.fromStack(dropped)).getMana() == 31_337,
				"dropped backpack keeps its mana, has " + TestSupport.tank(BackpackWrapper.fromStack(dropped)).getMana());
		helper.succeed();
	}

	@GameTest(template = TEMPLATE)
	public static void manaSurvivesRemovingAndReinstallingTheUpgradeItem(GameTestHelper helper) {
		ItemStack backpack = TestSupport.backpackWithTank(900);
		var handler = TestSupport.wrapper(backpack).getUpgradeHandler();
		int slot = -1;
		for (int i = 0; i < handler.getSlots(); i++) {
			if (!handler.getStackInSlot(i).isEmpty()) {
				slot = i;
			}
		}
		ItemStack taken = handler.extractItem(slot, 1, false);
		helper.assertTrue(!taken.isEmpty(), "upgrade extracted");
		helper.assertTrue(taken.getOrDefault(velrondevs.botaniabackpack.registry.ModDataComponents.MANA.get(), 0) == 900,
				"the taken upgrade item carries the mana in its data component");
		handler.insertItem(slot, taken, false);
		helper.assertTrue(TestSupport.tank(TestSupport.wrapper(backpack)).getMana() == 900, "mana is back after reinsertion");
		helper.succeed();
	}
}
