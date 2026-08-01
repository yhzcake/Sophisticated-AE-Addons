package cn.yhzcake.sophisticatedaeaddons;

import appeng.api.upgrades.Upgrades;
import cn.yhzcake.sophisticatedaeaddons.item.SophisticatedToolItem;
import cn.yhzcake.sophisticatedaeaddons.upgrade.AeBackpackUpgradeItem;
import cn.yhzcake.sophisticatedaeaddons.upgrade.AeStorageUpgradeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("null")
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SophisticatedAEAddons.MOD_ID);

    // Block items
    public static final DeferredHolder<Item, BlockItem> PLAYER_INTERFACE = ITEMS.register("player_interface",
        () -> new BlockItem(ModBlocks.PLAYER_INTERFACE.get(), new Item.Properties()));

    public static final DeferredHolder<Item, BlockItem> SOPHISTICATED_BACKPACK_INTERFACE = ITEMS.register("sophisticated_backpack_interface",
        () -> new BlockItem(ModBlocks.BACKPACK_INTERFACE.get(), new Item.Properties()));

    // Sophisticated Tool
    public static final DeferredHolder<Item, SophisticatedToolItem> SOPHISTICATED_TOOL = ITEMS.register("sophisticated_tool",
        () -> new SophisticatedToolItem(new Item.Properties().stacksTo(1)));

    // AE Upgrades
    public static final DeferredHolder<Item, AeBackpackUpgradeItem> AE_BACKPACK_UPGRADE = ITEMS.register("ae_backpack_upgrade",
        () -> new AeBackpackUpgradeItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, AeStorageUpgradeItem> AE_STORAGE_UPGRADE = ITEMS.register("ae_storage_upgrade",
        () -> new AeStorageUpgradeItem(new Item.Properties().stacksTo(16)));

    // AE2 card
    public static final DeferredHolder<Item, Item> PRECISE_PRIORITY_CARD = ITEMS.register("precise_priority_card",
        () -> Upgrades.createUpgradeCardItem(new Item.Properties().stacksTo(1)
            .component(ModDataComponents.PRIORITY_CONDITIONS, PriorityConditions.EMPTY)));

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
