package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.storage.ITerminalHost;
import appeng.menu.me.common.MEStorageMenu;
import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolSlots;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.AbstractContainerMenuAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MEStorageMenu.class, remap = false)
public abstract class MixinMEStorageMenu {
    @Unique
    private final List<Slot> sophisticatedAeAddons$toolSlots = new ArrayList<>();

    @Inject(
        method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ITerminalHost;Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/menu/me/common/MEStorageMenu;createPlayerInventorySlots(Lnet/minecraft/world/entity/player/Inventory;)V",
            shift = At.Shift.BEFORE
        ),
        require = 1
    )
    private void sophisticatedAeAddons$lockToolSlot(
        MenuType<?> menuType,
        int id,
        Inventory inventory,
        ITerminalHost host,
        boolean bindInventory,
        CallbackInfo ci
    ) {
        int toolSlot = SophisticatedToolSlots.findToolSlot(inventory);
        if (toolSlot >= 0) {
            ((MEStorageMenu) (Object) this).lockPlayerInventorySlot(toolSlot);
        }
    }

    @Inject(
        method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/api/storage/ITerminalHost;Z)V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$addToolSlots(
        MenuType<?> menuType,
        int id,
        Inventory inventory,
        ITerminalHost host,
        boolean bindInventory,
        CallbackInfo ci
    ) {
        int toolSlot = SophisticatedToolSlots.findToolSlot(inventory);
        if (toolSlot < 0) {
            return;
        }
        MEStorageMenu self = (MEStorageMenu) (Object) this;
        var handler = new cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolItemHandler(inventory, toolSlot);
        AbstractContainerMenuAccessor accessor = (AbstractContainerMenuAccessor) self;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            int column = slot % 3;
            int row = slot / 3;
            sophisticatedAeAddons$toolSlots.add(accessor.sophisticatedAeAddons$invokeAddSlot(
                new net.neoforged.neoforge.items.SlotItemHandler(
                    handler,
                    slot,
                    SophisticatedToolSlots.X + column * 18,
                    SophisticatedToolSlots.Y + row * 18
                )
            ));
        }
    }
}
