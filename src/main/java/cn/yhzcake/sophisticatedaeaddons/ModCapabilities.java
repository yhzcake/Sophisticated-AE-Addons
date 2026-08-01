package cn.yhzcake.sophisticatedaeaddons;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class ModCapabilities {
    private static final ItemStackHandler EMPTY = new ItemStackHandler(0);

    private ModCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.PLAYER_INTERFACE.get(),
            (be, side) ->
                be.getLevel() instanceof ServerLevel ? be.getItemHandler() : EMPTY);

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.BACKPACK_INTERFACE.get(),
            (be, side) ->
                be.getLevel() instanceof ServerLevel sl ? be.getItemHandler(sl) : EMPTY);
    }
}
