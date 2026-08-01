package cn.yhzcake.sophisticatedaeaddons.blockentity;

import cn.yhzcake.sophisticatedaeaddons.ModBlockEntities;
import cn.yhzcake.sophisticatedaeaddons.compat.BackpackCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class SophisticatedBackpackInterfaceBlockEntity extends BlockEntity {
    @Nullable
    private UUID boundBackpackUuid;
    private final IItemHandler itemHandler = new BoundBackpackItemHandler();
    @Nullable
    private IItemHandler resolvedItemHandler;
    private boolean handlerAvailable;
    private long inventorySignature;

    public SophisticatedBackpackInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BACKPACK_INTERFACE.get(), pos, state);
    }

    public void bindBackpack(@Nullable UUID uuid) {
        if (java.util.Objects.equals(boundBackpackUuid, uuid)) {
            return;
        }
        this.boundBackpackUuid = uuid;
        refreshBackpackInventory();
        setChanged();
        if (level != null) {
            level.invalidateCapabilities(worldPosition);
        }
    }

    @Nullable
    public UUID boundBackpackUuid() {
        return boundBackpackUuid;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BoundBackpack")) {
            boundBackpackUuid = tag.getUUID("BoundBackpack");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (boundBackpackUuid != null) {
            tag.putUUID("BoundBackpack", boundBackpackUuid);
        }
    }

    public IItemHandler getItemHandler(@Nullable ServerLevel level) {
        return itemHandler;
    }

    public static void serverTick(
        Level level,
        BlockPos pos,
        BlockState state,
        SophisticatedBackpackInterfaceBlockEntity blockEntity
    ) {
        if (level.getGameTime() % 20 == 0) {
            blockEntity.refreshBackpackInventory();
        }
    }

    private void refreshBackpackInventory() {
        IItemHandler handler = resolveItemHandler();
        boolean available = handler != null;
        long signature = available ? inventorySignature(handler) : 0;
        if (handlerAvailable != available || inventorySignature != signature) {
            handlerAvailable = available;
            inventorySignature = signature;
            resolvedItemHandler = handler;
            if (level != null) {
                level.invalidateCapabilities(worldPosition);
            }
        } else {
            resolvedItemHandler = handler;
        }
    }

    private long inventorySignature(IItemHandler handler) {
        long signature = handler.getSlots();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            signature = 31 * signature + handler.getStackInSlot(slot).hashCode();
        }
        return signature;
    }

    @Nullable
    private IItemHandler resolveItemHandler() {
        if (boundBackpackUuid == null || !(level instanceof ServerLevel serverLevel) || !BackpackCompat.isLoaded()) {
            return null;
        }
        return BackpackCompat.findBackpackInventory(serverLevel, getBlockPos(), boundBackpackUuid);
    }

    private final class BoundBackpackItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            IItemHandler handler = resolvedItemHandler;
            return handler == null ? 0 : handler.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            IItemHandler handler = resolvedItemHandler;
            return handler == null || slot < 0 || slot >= handler.getSlots() ? ItemStack.EMPTY : handler.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            IItemHandler handler = resolvedItemHandler;
            if (handler == null || slot < 0 || slot >= handler.getSlots()) {
                return stack;
            }
            ItemStack remainder = handler.insertItem(slot, stack, simulate);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler handler = resolvedItemHandler;
            if (handler == null || slot < 0 || slot >= handler.getSlots()) {
                return ItemStack.EMPTY;
            }
            ItemStack extracted = handler.extractItem(slot, amount, simulate);
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler handler = resolvedItemHandler;
            return handler == null || slot < 0 || slot >= handler.getSlots() ? 0 : handler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            IItemHandler handler = resolvedItemHandler;
            return handler != null && slot >= 0 && slot < handler.getSlots() && handler.isItemValid(slot, stack);
        }
    }
}
