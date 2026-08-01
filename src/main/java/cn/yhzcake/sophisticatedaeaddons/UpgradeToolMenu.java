package cn.yhzcake.sophisticatedaeaddons;

import cn.yhzcake.sophisticatedaeaddons.item.SophisticatedToolItem;
import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolItemHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;

public final class UpgradeToolMenu extends AbstractContainerMenu {
    private static final int UPGRADE_SLOTS = UpgradeToolContents.SLOTS;
    private final int toolSlot;

    public UpgradeToolMenu(int containerId, Inventory inventory, int toolSlot) {
        super(ModMenus.UPGRADE_TOOL.get(), containerId);
        this.toolSlot = toolSlot;
        SophisticatedToolItemHandler upgrades = new SophisticatedToolItemHandler(inventory, toolSlot);

        // Tool upgrade slots
        for (int i = 0; i < UPGRADE_SLOTS; i++) {
            int row = i / 3;
            int col = i % 3;
            addSlot(new UpgradeSlot(upgrades, i, 62 + col * 18, 17 + row * 18));
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int inventorySlot = col + row * 9 + 9;
                addSlot(new PlayerSlot(inventory, inventorySlot, 8 + col * 18, 84 + row * 18, toolSlot));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new PlayerSlot(inventory, col, 8 + col * 18, 142, toolSlot));
        }
    }

    public UpgradeToolMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readVarInt());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();

        if (index < UPGRADE_SLOTS) {
            // Move from upgrade slots to player inventory
            if (!moveItemStackTo(stackInSlot, UPGRADE_SLOTS, UPGRADE_SLOTS + 36, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Move from player inventory to upgrade slots (only valid upgrades)
            if (isUpgradeItem(stackInSlot)) {
                if (!moveItemStackTo(stackInSlot, 0, UPGRADE_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        boolean targetIsTool = slotId >= 0
            && slotId < slots.size()
            && slots.get(slotId).container == player.getInventory()
            && slots.get(slotId).getSlotIndex() == toolSlot;
        boolean swapsWithTool = clickType == ClickType.SWAP
            && (button == toolSlot || toolSlot == 40 && button == 40);
        if (targetIsTool || swapsWithTool) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private static boolean isUpgradeItem(ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem<?>;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(toolSlot).is(ModItems.SOPHISTICATED_TOOL.get());
    }

    private static final class UpgradeSlot extends SlotItemHandler {
        UpgradeSlot(SophisticatedToolItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isUpgradeItem(stack);
        }
    }

    private static final class PlayerSlot extends Slot {
        private final int lockedSlot;

        private PlayerSlot(Inventory inventory, int index, int x, int y, int lockedSlot) {
            super(inventory, index, x, y);
            this.lockedSlot = lockedSlot;
        }

        @Override
        public boolean mayPickup(Player player) {
            return getSlotIndex() != lockedSlot;
        }
    }
}
