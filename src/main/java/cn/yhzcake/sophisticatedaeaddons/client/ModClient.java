package cn.yhzcake.sophisticatedaeaddons.client;

import cn.yhzcake.sophisticatedaeaddons.ModMenus;
import cn.yhzcake.sophisticatedaeaddons.screen.UpgradeToolScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = "sophisticated_ae_addons", dist = Dist.CLIENT)
public final class ModClient {
    static {
        // Static init to register client-side handlers
    }

    public ModClient(IEventBus modBus) {
        modBus.addListener(ModClient::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.UPGRADE_TOOL.get(), UpgradeToolScreen::new);
    }
}
