package cn.yhzcake.sophisticatedaeaddons.inventory;

import cn.yhzcake.sophisticatedaeaddons.upgrade.AeNetworkAccess;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("null")
public final class AeUpgradeInventoryHandler implements ITrackedContentsItemHandler {
    private final IStorageWrapper storageWrapper;
    private final ITrackedContentsItemHandler local;
    private final List<ItemStack> aeStacks;

    public AeUpgradeInventoryHandler(IStorageWrapper storageWrapper, ITrackedContentsItemHandler local) {
        this.storageWrapper = storageWrapper;
        this.local = local;
        this.aeStacks = AeNetworkAccess.getAvailableItems(storageWrapper);
    }

    @Override
    public int getSlots() {
        return local.getSlots() + aeStacks.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            return ItemStack.EMPTY;
        }
        if (slot < local.getSlots()) {
            return local.getStackInSlot(slot);
        }
        return aeStacks.get(slot - local.getSlots()).copy();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= getSlots()) {
            return stack;
        }
        return slot < local.getSlots() ? local.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot >= getSlots() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        if (slot < local.getSlots()) {
            return local.extractItem(slot, amount, simulate);
        }
        int aeSlot = slot - local.getSlots();
        ItemStack available = aeStacks.get(aeSlot);
        ItemStack extracted = AeNetworkAccess.extract(storageWrapper, available, amount, simulate);
        if (!simulate && !extracted.isEmpty()) {
            available.shrink(extracted.getCount());
        }
        return extracted;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot < 0 || slot >= getSlots()) {
            return 0;
        }
        return slot < local.getSlots() ? local.getSlotLimit(slot) : Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot >= 0 && slot < local.getSlots() && local.isItemValid(slot, stack);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= getSlots()) {
            throw new IndexOutOfBoundsException("Slot " + slot);
        }
        if (slot < local.getSlots()) {
            local.setStackInSlot(slot, stack);
            return;
        }
        int aeSlot = slot - local.getSlots();
        ItemStack available = aeStacks.get(aeSlot);
        if ((!stack.isEmpty() && !ItemStack.isSameItemSameComponents(available, stack))
            || stack.getCount() > available.getCount()) {
            return;
        }
        int amount = available.getCount() - stack.getCount();
        if (amount > 0) {
            ItemStack extracted = AeNetworkAccess.extract(storageWrapper, available, amount, false);
            available.shrink(extracted.getCount());
        }
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return local.insertItem(stack, simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        ItemStack localExtracted = local.extractItem(stack, simulate);
        return AeNetworkAccess.extractMissing(storageWrapper, stack, localExtracted, simulate);
    }

    @Override
    public Set<ItemStackKey> getTrackedStacks() {
        Set<ItemStackKey> tracked = new HashSet<>(local.getTrackedStacks());
        for (ItemStack stack : aeStacks) {
            tracked.add(ItemStackKey.of(stack));
        }
        return tracked;
    }

    @Override
    public void registerTrackingListeners(
        Consumer<ItemStackKey> onAddStackKey,
        Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot,
        Runnable onRemoveLastEmptySlot
    ) {
        local.registerTrackingListeners(onAddStackKey, onRemoveStackKey, onAddFirstEmptySlot, onRemoveLastEmptySlot);
    }

    @Override
    public void unregisterStackKeyListeners() {
        local.unregisterStackKeyListeners();
    }

    @Override
    public boolean hasEmptySlots() {
        return local.hasEmptySlots();
    }

    @Override
    public boolean isInsertBlocked() {
        return local.isInsertBlocked();
    }
}
