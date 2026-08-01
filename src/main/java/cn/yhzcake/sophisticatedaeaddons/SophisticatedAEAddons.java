package cn.yhzcake.sophisticatedaeaddons;

import appeng.api.upgrades.Upgrades;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AEComponents;
import appeng.core.definitions.AEParts;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@SuppressWarnings("null")
@Mod(SophisticatedAEAddons.MOD_ID)
public final class SophisticatedAEAddons {
    public static final String MOD_ID = "sophisticated_ae_addons";

    public SophisticatedAEAddons(IEventBus modBus, ModContainer container) {
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenus.register(modBus);
        ModCreativeTab.register(modBus);
        modBus.addListener(ModCapabilities::registerCapabilities);
        modBus.addListener(ModNetwork::register);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(ModItems.PRECISE_PRIORITY_CARD.get(), AEParts.STORAGE_BUS, 1);
            IGridLinkableHandler linkHandler = new IGridLinkableHandler() {
                @Override
                public boolean canLink(ItemStack stack) {
                    return stack.is(ModItems.AE_BACKPACK_UPGRADE.get())
                        || stack.is(ModItems.AE_STORAGE_UPGRADE.get());
                }

                @Override
                public void link(ItemStack stack, GlobalPos pos) {
                    stack.set(AEComponents.WIRELESS_LINK_TARGET, pos);
                }

                @Override
                public void unlink(ItemStack stack) {
                    stack.remove(AEComponents.WIRELESS_LINK_TARGET);
                }
            };
            GridLinkables.register(ModItems.AE_BACKPACK_UPGRADE.get(), linkHandler);
            GridLinkables.register(ModItems.AE_STORAGE_UPGRADE.get(), linkHandler);
        });
    }
}
