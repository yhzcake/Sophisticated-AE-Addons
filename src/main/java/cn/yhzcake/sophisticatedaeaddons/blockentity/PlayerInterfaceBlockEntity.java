package cn.yhzcake.sophisticatedaeaddons.blockentity;

import cn.yhzcake.sophisticatedaeaddons.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PlayerInterfaceBlockEntity extends BlockEntity {
    @Nullable
    private UUID boundPlayerUuid;
    private final PlayerInventoryWrapper itemHandler = new PlayerInventoryWrapper(this);

    public PlayerInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYER_INTERFACE.get(), pos, state);
    }

    public void bindPlayer(UUID uuid) {
        this.boundPlayerUuid = uuid;
        setChanged();
    }

    @Nullable
    public UUID boundPlayerUuid() {
        return boundPlayerUuid;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BoundPlayer")) {
            boundPlayerUuid = tag.getUUID("BoundPlayer");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (boundPlayerUuid != null) {
            tag.putUUID("BoundPlayer", boundPlayerUuid);
        }
    }

    /**
     * Returns an IItemHandler that bridges the player's inventory.
     * Uses ItemStack copies to avoid corrupting the player's actual items.
     */
    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    private static final class PlayerInventoryWrapper implements IItemHandler {
        private final PlayerInterfaceBlockEntity owner;
        // main(36) + armor(4) + offhand(1) + ender(27)
        private static final int MAIN = 36;
        private static final int ARMOR = 4;
        private static final int OFFHAND = 1;
        private static final int ENDER = 27;
        private static final int TOTAL = MAIN + ARMOR + OFFHAND + ENDER;

        PlayerInventoryWrapper(PlayerInterfaceBlockEntity owner) {
            this.owner = owner;
        }

        @Override
        public int getSlots() {
            return TOTAL;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return getSource(slot).copy();
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack toInsert, boolean simulate) {
            if (toInsert.isEmpty()) return ItemStack.EMPTY;
            if (player() == null) return toInsert;
            ItemStack existing = getSource(slot);
            int limit = Math.min(getSlotLimit(slot), existing.isEmpty() ? toInsert.getMaxStackSize() : existing.getMaxStackSize());
            if (existing.isEmpty()) {
                int amount = Math.min(toInsert.getCount(), limit);
                if (!simulate) {
                    setSlot(slot, toInsert.copyWithCount(amount));
                }
                ItemStack remainder = toInsert.copy();
                remainder.shrink(amount);
                return remainder;
            }
            if (!ItemStack.isSameItemSameComponents(existing, toInsert)) return toInsert;
            int combined = existing.getCount() + toInsert.getCount();
            int accepted = Math.min(combined, limit) - existing.getCount();
            if (accepted <= 0) return toInsert;
            if (!simulate) {
                existing.grow(accepted);
                setSlot(slot, existing);
            }
            ItemStack remainder = toInsert.copy();
            remainder.shrink(accepted);
            return remainder;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0) return ItemStack.EMPTY;
            ItemStack existing = getSource(slot);
            if (existing.isEmpty()) return ItemStack.EMPTY;
            int toExtract = Math.min(amount, existing.getCount());
            ItemStack result = existing.copyWithCount(toExtract);
            if (!simulate) {
                existing.shrink(toExtract);
                setSlot(slot, existing);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return player() != null;
        }

        private ItemStack getSource(int slot) {
            ServerPlayer player = player();
            if (player == null) return ItemStack.EMPTY;
            Inventory inv = player.getInventory();
            if (slot < MAIN) return inv.getItem(slot).copy();
            int s = slot - MAIN;
            if (s < ARMOR) return inv.getArmor(s).copy();
            s -= ARMOR;
            if (s < OFFHAND) return inv.offhand.get(0).copy();
            s -= OFFHAND;
            var ender = player.getEnderChestInventory();
            return s < ENDER ? ender.getItem(s).copy() : ItemStack.EMPTY;
        }

        private void setSlot(int slot, ItemStack stack) {
            ServerPlayer player = player();
            if (player == null) return;
            Inventory inv = player.getInventory();
            if (slot < MAIN) { inv.setItem(slot, stack); return; }
            int s = slot - MAIN;
            if (s < ARMOR) { inv.armor.set(s, stack); return; }
            s -= ARMOR;
            if (s < OFFHAND) { inv.offhand.set(0, stack); return; }
            s -= OFFHAND;
            var ender = player.getEnderChestInventory();
            if (s < ENDER) { ender.setItem(s, stack); }
        }

        @Nullable
        private ServerPlayer player() {
            if (!(owner.getLevel() instanceof ServerLevel level) || owner.boundPlayerUuid == null) {
                return null;
            }
            return level.getServer().getPlayerList().getPlayer(owner.boundPlayerUuid);
        }
    }
}
