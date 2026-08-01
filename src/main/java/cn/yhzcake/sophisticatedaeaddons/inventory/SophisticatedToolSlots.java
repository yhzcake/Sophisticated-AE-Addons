package cn.yhzcake.sophisticatedaeaddons.inventory;

import cn.yhzcake.sophisticatedaeaddons.ModItems;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.SlotAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.List;

public final class SophisticatedToolSlots {
    public static final int X = -67;
    public static final int Y = 18;
    private static final int PLAYER_INVENTORY_X_OFFSET = -63;
    private static final int PLAYER_INVENTORY_Y_OFFSET = 16;

    private SophisticatedToolSlots() {
    }

    public static int findToolSlot(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(ModItems.SOPHISTICATED_TOOL.get())) {
                return slot;
            }
        }
        return -1;
    }

    public static Slot positionNextToPlayerInventory(List<Slot> slots) {
        int playerInventoryX = Integer.MAX_VALUE;
        int playerInventoryY = Integer.MAX_VALUE;
        for (Slot slot : slots) {
            if (slot.container instanceof Inventory && slot.getSlotIndex() >= 9 && slot.getSlotIndex() < 36) {
                playerInventoryX = Math.min(playerInventoryX, slot.x);
                playerInventoryY = Math.min(playerInventoryY, slot.y);
            }
        }
        if (playerInventoryX == Integer.MAX_VALUE || playerInventoryY == Integer.MAX_VALUE) {
            return null;
        }
        Slot firstToolSlot = null;
        for (Slot slot : slots) {
            if (!(slot instanceof SlotItemHandler itemHandlerSlot)
                || !(itemHandlerSlot.getItemHandler() instanceof SophisticatedToolItemHandler)) {
                continue;
            }
            int toolSlot = itemHandlerSlot.getSlotIndex();
            SlotAccessor accessor = (SlotAccessor) slot;
            accessor.sophisticatedAeAddons$setX(playerInventoryX + PLAYER_INVENTORY_X_OFFSET + toolSlot % 3 * 18);
            accessor.sophisticatedAeAddons$setY(playerInventoryY + PLAYER_INVENTORY_Y_OFFSET + toolSlot / 3 * 18);
            if (toolSlot == 0) {
                firstToolSlot = slot;
            }
        }
        return firstToolSlot;
    }

}
