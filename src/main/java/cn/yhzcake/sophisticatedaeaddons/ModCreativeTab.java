package cn.yhzcake.sophisticatedaeaddons;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("null")
public final class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SophisticatedAEAddons.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sophisticated_ae_addons"))
            .icon(() -> new ItemStack(ModItems.SOPHISTICATED_TOOL.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.PLAYER_INTERFACE.get());
                output.accept(ModItems.SOPHISTICATED_BACKPACK_INTERFACE.get());
                output.accept(ModItems.SOPHISTICATED_TOOL.get());
                output.accept(ModItems.AE_BACKPACK_UPGRADE.get());
                output.accept(ModItems.AE_STORAGE_UPGRADE.get());
                output.accept(ModItems.PRECISE_PRIORITY_CARD.get());
            }).build());

    private ModCreativeTab() {}

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
