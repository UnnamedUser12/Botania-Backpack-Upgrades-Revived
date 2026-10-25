package velrondevs.botaniabackpack.datagen.providers.lang;

import velrondevs.botaniabackpack.BotaniaBackpackUpgrades;
import velrondevs.botaniabackpack.registry.ModCreativeTabs;
import velrondevs.botaniabackpack.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public final class ZhCnLanguageProvider extends LanguageProvider {
	public ZhCnLanguageProvider(PackOutput output) {
		super(output, BotaniaBackpackUpgrades.MOD_ID, "zh_cn");
	}

	@Override
	protected void addTranslations() {
		addItem(ModItems.MANA_TANK_UPGRADE, "魔力储罐升级");
		add("item.botaniabackpack.mana_tank_upgrade.tooltip",
				"背包放置后可存储魔力。\n可作为植物魔法的魔力池，与魔力脉冲、发射器、火花和耗魔设备交互。");
		add("gui.botaniabackpack.status.add.mana_tank_exists", "每个背包只能安装一个魔力储罐升级。");
		add("gui.botaniabackpack.upgrade.mana_tank", "魔力储罐");
		add("gui.botaniabackpack.upgrade.mana_tank.tooltip", "设置魔力传输方向");
		add("gui.botaniabackpack.transfer_mode.both", "输入与输出");
		add("gui.botaniabackpack.transfer_mode.input", "仅输入");
		add("gui.botaniabackpack.transfer_mode.output", "仅输出");
		add("gui.botaniabackpack.mana_tank.contents", "%s / %s 魔力");
		add("gui.botaniabackpack.mana_tank.interaction", "用魔力物品右击以充放魔力；方向遵循传输模式");

		addItem(ModItems.MANA_SUPPLY_UPGRADE, "魔力供给升级");
		add("item.botaniabackpack.mana_supply_upgrade.tooltip",
				"使玩家携带的兼容耗魔装备能够按需使用同一背包魔力储罐中的魔力。\n不会自动给背包库存中的物品充能。");
		add("gui.botaniabackpack.status.add.mana_supply_exists", "每个背包只能安装一个魔力供给升级。");

		addItem(ModItems.MANA_INFUSION_UPGRADE, "魔力灌注升级");
		add("item.botaniabackpack.mana_infusion_upgrade.tooltip",
				"使用同一背包的魔力储罐，将过滤器选中的物品按魔力灌注配方转化。\n仅支持不需要催化器的配方。");
		addItem(ModItems.ADVANCED_MANA_INFUSION_UPGRADE, "高级魔力灌注升级");
		add("item.botaniabackpack.advanced_mana_infusion_upgrade.tooltip",
				"使用同一背包的魔力储罐进行魔力灌注。\n背包方块下方的炼金或凝聚催化器可启用对应配方。\n拥有更多过滤选项。");
		add("gui.botaniabackpack.status.add.mana_infusion_exists", "每个背包只能安装一个魔力灌注升级。");
		add("gui.botaniabackpack.upgrade.mana_infusion", "魔力灌注");
		add("gui.botaniabackpack.upgrade.mana_infusion.tooltip", "魔力灌注设置");
		add("gui.botaniabackpack.upgrade.advanced_mana_infusion", "高级魔力灌注");
		add("gui.botaniabackpack.upgrade.advanced_mana_infusion.tooltip", "高级魔力灌注设置");

		add("fluid_type.botaniabackpack.mana_display", "魔力");
		add(ModCreativeTabs.MAIN_TAB_TRANSLATION_KEY, "植物魔法背包升级");
	}
}
