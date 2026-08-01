package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolItemHandler;
import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolSlots;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.StorageContainerMenuBaseAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = StorageContainerMenuBase.class, remap = false)
public abstract class MixinStorageContainerMenuBase {
    private int sophisticatedAeAddons$toolInventorySlot = -1;

    @Inject(
        method = "initSlotsAndContainers(Lnet/minecraft/world/entity/player/Player;IZLjava/util/List;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/common/gui/StorageContainerMenuBase;addPlayerInventorySlots(Lnet/minecraft/world/entity/player/Inventory;IZ)V",
            shift = At.Shift.BEFORE
        ),
        require = 1
    )
    private void sophisticatedAeAddons$addToolSlots(
        Player player,
        int storageItemSlotIndex,
        boolean shouldLockStorageItemSlot,
        List<Slot> extraSlots,
        CallbackInfo ci
    ) {
        Inventory inventory = player.getInventory();
        int toolSlot = SophisticatedToolSlots.findToolSlot(inventory);
        if (toolSlot < 0) {
            return;
        }
        sophisticatedAeAddons$toolInventorySlot = toolSlot;
        StorageContainerMenuBase<?> self = (StorageContainerMenuBase<?>) (Object) this;
        StorageContainerMenuBaseAccessor accessor = (StorageContainerMenuBaseAccessor) self;
        SophisticatedToolItemHandler handler = new SophisticatedToolItemHandler(inventory, toolSlot);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            int column = slot % 3;
            int row = slot / 3;
            accessor.sophisticatedAeAddons$invokeAddExtraSlot(new SlotItemHandler(
                handler,
                slot,
                SophisticatedToolSlots.X + column * 18,
                SophisticatedToolSlots.Y + row * 18
            ));
        }
    }

    @Inject(
        method = "clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$lockToolItem(
        int slotId,
        int button,
        ClickType clickType,
        Player player,
        CallbackInfo ci
    ) {
        StorageContainerMenuBase<?> self = (StorageContainerMenuBase<?>) (Object) this;
        if (sophisticatedAeAddons$toolInventorySlot < 0) {
            return;
        }
        boolean targetIsTool = slotId >= 0
            && slotId < self.slots.size()
            && self.slots.get(slotId).container == player.getInventory()
            && self.slots.get(slotId).getSlotIndex() == sophisticatedAeAddons$toolInventorySlot;
        boolean swapsWithTool = clickType == ClickType.SWAP
            && (button == sophisticatedAeAddons$toolInventorySlot
                || sophisticatedAeAddons$toolInventorySlot == 40 && button == 40);
        if (targetIsTool || swapsWithTool) {
            ci.cancel();
        }
    }
}
