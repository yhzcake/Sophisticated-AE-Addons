package cn.yhzcake.sophisticatedaeaddons.inventory;

import cn.yhzcake.sophisticatedaeaddons.UpgradeToolContents;
import cn.yhzcake.sophisticatedaeaddons.ModItems;
import cn.yhzcake.sophisticatedaeaddons.item.SophisticatedToolItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;

public final class SophisticatedToolItemHandler implements IItemHandlerModifiable {
    private final Inventory inventory;
    private final int toolSlot;

    public SophisticatedToolItemHandler(Inventory inventory, int toolSlot) {
        this.inventory = inventory;
        this.toolSlot = toolSlot;
    }

    @Override
    public int getSlots() {
        return UpgradeToolContents.SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= getSlots() || !isValid()) {
            return ItemStack.EMPTY;
        }
        return contents().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || !isValid() || !isItemValid(slot, stack)) {
            return stack;
        }
        ItemStack existing = getStackInSlot(slot);
        int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            return stack;
        }
        int accepted = Math.min(stack.getCount(), limit - existing.getCount());
        if (accepted <= 0) {
            return stack;
        }
        if (!simulate) {
            ItemStack result = existing.isEmpty()
                ? stack.copyWithCount(accepted)
                : existing.copyWithCount(existing.getCount() + accepted);
            setStackInSlot(slot, result);
        }
        return stack.copyWithCount(stack.getCount() - accepted);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || amount <= 0 || !isValid()) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate) {
            setStackInSlot(slot, existing.copyWithCount(existing.getCount() - extracted));
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.getItem() instanceof IUpgradeItem<?>;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IndexOutOfBoundsException("Slot " + slot);
        }
        if (!isValid()) {
            return;
        }
        SophisticatedToolItem.setContents(
            toolStack(),
            contents().setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack)
        );
        inventory.setChanged();
    }

    public int toolSlot() {
        return toolSlot;
    }

    private ItemStack toolStack() {
        ItemStack current = inventory.getItem(toolSlot);
        if (!current.is(ModItems.SOPHISTICATED_TOOL.get())) {
            return ItemStack.EMPTY;
        }
        return current;
    }

    private boolean isValid() {
        return !toolStack().isEmpty();
    }

    private UpgradeToolContents contents() {
        return SophisticatedToolItem.getContents(toolStack());
    }
}
