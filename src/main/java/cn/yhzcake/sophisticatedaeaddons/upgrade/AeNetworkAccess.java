package cn.yhzcake.sophisticatedaeaddons.upgrade;

import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;

import java.util.ArrayList;
import java.util.List;

public final class AeNetworkAccess {
    private AeNetworkAccess() {
    }

    public static ItemStack extractMissing(
        IStorageWrapper storageWrapper,
        ItemStack requested,
        ItemStack locallyExtracted,
        boolean simulate
    ) {
        int missing = requested.getCount() - locallyExtracted.getCount();
        if (missing <= 0) {
            return locallyExtracted;
        }
        ItemStack extracted = extract(storageWrapper, requested, missing, simulate);
        if (extracted.isEmpty()) {
            return locallyExtracted;
        }
        if (locallyExtracted.isEmpty()) {
            return extracted;
        }
        ItemStack combined = locallyExtracted.copy();
        combined.grow(extracted.getCount());
        return combined;
    }

    public static ItemStack extract(IStorageWrapper storageWrapper, ItemStack requested, int amount, boolean simulate) {
        if (requested.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }
        BoundGrid boundGrid = findBoundGrid(storageWrapper);
        if (boundGrid == null) {
            return ItemStack.EMPTY;
        }
        long extracted = boundGrid.grid().getStorageService().getInventory().extract(
            AEItemKey.of(requested),
            amount,
            simulate ? Actionable.SIMULATE : Actionable.MODULATE,
            IActionSource.ofMachine(boundGrid.accessPoint())
        );
        return extracted <= 0 ? ItemStack.EMPTY : requested.copyWithCount((int) extracted);
    }

    public static List<ItemStack> getAvailableItems(IStorageWrapper storageWrapper) {
        BoundGrid boundGrid = findBoundGrid(storageWrapper);
        if (boundGrid == null) {
            return List.of();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (var entry : boundGrid.grid().getStorageService().getInventory().getAvailableStacks()) {
            if (entry.getKey() instanceof AEItemKey itemKey && entry.getLongValue() > 0) {
                stacks.add(itemKey.toStack((int) Math.min(Integer.MAX_VALUE, entry.getLongValue())));
            }
        }
        return stacks;
    }

    private static BoundGrid findBoundGrid(IStorageWrapper storageWrapper) {
        for (IUpgradeWrapper wrapper : storageWrapper.getUpgradeHandler().getSlotWrappers().values()) {
            if (!wrapper.isEnabled()) {
                continue;
            }
            if (!(wrapper instanceof AeBackpackUpgradeWrapper)
                && !(wrapper instanceof AeStorageUpgradeWrapper)) {
                continue;
            }
            GlobalPos target = wrapper.getUpgradeStack().get(AEComponents.WIRELESS_LINK_TARGET);
            BoundGrid grid = resolve(target);
            if (grid != null) {
                return grid;
            }
        }
        return null;
    }

    private static BoundGrid resolve(GlobalPos target) {
        if (target == null) {
            return null;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        ServerLevel level = server.getLevel(target.dimension());
        if (level == null || !level.hasChunkAt(target.pos())) {
            return null;
        }
        if (!(level.getBlockEntity(target.pos()) instanceof IWirelessAccessPoint accessPoint)
            || !accessPoint.isActive()) {
            return null;
        }
        IGrid grid = accessPoint.getGrid();
        return grid == null ? null : new BoundGrid(grid, accessPoint);
    }

    private record BoundGrid(IGrid grid, IWirelessAccessPoint accessPoint) {
    }
}
