package cn.yhzcake.sophisticatedaeaddons;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("null")
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, SophisticatedAEAddons.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<UpgradeToolMenu>> UPGRADE_TOOL = MENUS.register("upgrade_tool", () -> IMenuTypeExtension.create(UpgradeToolMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}

