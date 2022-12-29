package gg.moonflower.tolerablecreepers.core.registry;

import gg.moonflower.pollen.api.registry.PollinatedRegistry;
import gg.moonflower.tolerablecreepers.common.item.CreeperSporesItem;
import gg.moonflower.tolerablecreepers.common.entity.MischiefArrow;
import gg.moonflower.tolerablecreepers.common.item.MischiefArrowItem;
import gg.moonflower.tolerablecreepers.core.TolerableCreepers;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class TCItems {
    public static final PollinatedRegistry<Item> ITEMS = PollinatedRegistry.create(Registry.ITEM, TolerableCreepers.MOD_ID);

    public static final Supplier<Item> CREEPER_SPORES = ITEMS.register("creeper_spores", () -> new CreeperSporesItem(new Item.Properties().tab(CreativeModeTab.TAB_MISC)));
    public static final Supplier<Item> MISCHIEF_ARROW = ITEMS.register("mischief_arrow", () -> new MischiefArrowItem(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));
}
