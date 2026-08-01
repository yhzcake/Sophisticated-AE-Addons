package cn.yhzcake.sophisticatedaeaddons.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackBlockEntity;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackAccessLogger;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Safe bridge to Sophisticated Backpacks API.
 * All calls are guarded by {@link #isLoaded()}.
 */
@SuppressWarnings("null")
public final class BackpackCompat {

    public static boolean isLoaded() {
        return ModList.get().isLoaded("sophisticatedbackpacks");
    }

    public static boolean isBackpack(ItemStack stack) {
        return isLoaded() && stack.getItem() instanceof BackpackItem;
    }

    @Nullable
    public static UUID getBackpackUuid(ItemStack stack) {
        if (!isLoaded()) return null;
        var opt = BackpackWrapper.fromExistingData(stack);
        return opt.flatMap(IBackpackWrapper::getContentsUuid).orElse(null);
    }

    @Nullable
    public static UUID getOrCreateBackpackUuid(ItemStack stack) {
        if (!isBackpack(stack)) return null;
        IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
        BackpackWrapperIndex.register(wrapper);
        return wrapper.getContentsUuid().orElse(null);
    }

    @Nullable
    public static IItemHandler findBackpackInventory(ServerLevel level, BlockPos center, UUID uuid) {
        if (!isLoaded() || uuid == null) return null;

        IBackpackWrapper indexedWrapper = BackpackWrapperIndex.find(uuid);
        if (indexedWrapper != null) {
            return indexedWrapper.getInventoryForInputOutput();
        }

        var accessLog = BackpackAccessLogger.getBackpackLog(uuid);
        if (accessLog.isPresent()) {
            var log = accessLog.get();
            ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(log.getBackpackItemRegistryName()));
            IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
            wrapper.setColors(log.getClothColor(), log.getTrimColor());
            wrapper.setColumnsTaken(log.getColumnsTaken(), false);
            wrapper.setContentsUuid(uuid);
            BackpackWrapperIndex.register(wrapper);
            return wrapper.getInventoryForInputOutput();
        }

        // Search chunks around the interface
        int cx = Math.floorDiv(center.getX(), 16);
        int cz = Math.floorDiv(center.getZ(), 16);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (!level.hasChunk(cx + dx, cz + dz)) continue;
                LevelChunk chunk = level.getChunk(cx + dx, cz + dz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof BackpackBlockEntity backpack) {
                        if (uuid.equals(backpack.getBackpackWrapper().getContentsUuid().orElse(null))) {
                            return backpack.getBackpackWrapper().getInventoryForInputOutput();
                        }
                    }
                }
            }
        }

        // Search player inventories
        for (var player : level.players()) {
            for (ItemStack stack : player.getInventory().items) {
                IItemHandler h = findInStack(stack, uuid);
                if (h != null) return h;
            }
            for (ItemStack stack : player.getInventory().armor) {
                IItemHandler h = findInStack(stack, uuid);
                if (h != null) return h;
            }
            IItemHandler h = findInStack(player.getOffhandItem(), uuid);
            if (h != null) return h;
            for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
                h = findInStack(player.getEnderChestInventory().getItem(slot), uuid);
                if (h != null) return h;
            }
        }
        return null;
    }

    @Nullable
    private static IItemHandler findInStack(ItemStack stack, UUID uuid) {
        if (stack.isEmpty()) return null;
        if (!(stack.getItem() instanceof BackpackItem)) return null;
        IBackpackWrapper wrapper = BackpackWrapper.fromStack(stack);
        BackpackWrapperIndex.register(wrapper);
        if (uuid.equals(wrapper.getContentsUuid().orElse(null))) {
            return wrapper.getInventoryForInputOutput();
        }
        return null;
    }

    private BackpackCompat() {}
}
