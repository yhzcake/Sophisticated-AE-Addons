package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import cn.yhzcake.sophisticatedaeaddons.priority.ConditionalPriorityStorage;
import cn.yhzcake.sophisticatedaeaddons.priority.MigrationRoutingContext;
import cn.yhzcake.sophisticatedaeaddons.priority.NetworkInsertionContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.NavigableMap;

@Mixin(value = NetworkStorage.class, remap = false)
public abstract class MixinNetworkStorage {
    @Shadow
    private boolean mountsInUse;

    @Shadow
    @Final
    private NavigableMap<Integer, List<MEStorage>> priorityInventory;

    @Shadow
    protected abstract boolean isQueuedForRemoval(MEStorage storage);

    @Shadow
    protected abstract void flushQueuedOperations();

    @Inject(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$insertWithConditionalPriority(
        AEKey key,
        long amount,
        Actionable mode,
        IActionSource source,
        CallbackInfoReturnable<Long> cir
    ) {
        boolean migrationReentry = mountsInUse;
        if (migrationReentry && !MigrationRoutingContext.enterNetworkRouting()) {
            cir.setReturnValue(0L);
            return;
        }

        boolean hasActiveConditionalStorage = priorityInventory.values().stream()
            .flatMap(List::stream)
            .filter(ConditionalPriorityStorage.class::isInstance)
            .map(ConditionalPriorityStorage.class::cast)
            .anyMatch(ConditionalPriorityStorage::sophisticatedAeAddons$isConditionActive);
        if (!migrationReentry && !hasActiveConditionalStorage) {
            return;
        }

        long remaining = amount;
        if (!migrationReentry) {
            NetworkStorage self = (NetworkStorage) (Object) this;
            boolean requiresNetworkSnapshot = priorityInventory.values().stream()
                .flatMap(List::stream)
                .filter(ConditionalPriorityStorage.class::isInstance)
                .map(ConditionalPriorityStorage.class::cast)
                .anyMatch(storage -> storage.sophisticatedAeAddons$isConditionActive()
                    && storage.sophisticatedAeAddons$requiresNetworkSnapshot());
            long stored = requiresNetworkSnapshot ? self.getAvailableStacks().get(key) : 0;
            NetworkInsertionContext.begin(key, stored, amount);
        }
        if (!migrationReentry) {
            mountsInUse = true;
        }
        boolean preflightRejected = false;
        try {
            if (mode == Actionable.MODULATE) {
                for (var priorityEntry : priorityInventory.entrySet()) {
                    for (MEStorage storage : priorityEntry.getValue()) {
                        if (storage instanceof ConditionalPriorityStorage conditional
                            && conditional.sophisticatedAeAddons$isConditionActive()
                            && !isQueuedForRemoval(storage)) {
                            storage.insert(key, remaining, Actionable.SIMULATE, source);
                            if (NetworkInsertionContext.isFallbackBlocked(key)) {
                                preflightRejected = true;
                                break;
                            }
                        }
                    }
                    if (preflightRejected) {
                        break;
                    }
                }
            }
            if (!preflightRejected) {
                for (var priorityEntry : priorityInventory.entrySet()) {
                List<MEStorage> storages = priorityEntry.getValue();
                for (MEStorage storage : storages) {
                    if (remaining <= 0) {
                        break;
                    }
                    if (storage instanceof ConditionalPriorityStorage conditional
                        && conditional.sophisticatedAeAddons$isConditionActive()
                        && !isQueuedForRemoval(storage)) {
                        long inserted = storage.insert(key, remaining, mode, source);
                        remaining -= inserted;
                    }
                }
            }

            if (!preflightRejected && !NetworkInsertionContext.isFallbackBlocked(key)) {
                for (var priorityEntry : priorityInventory.entrySet()) {
                    List<MEStorage> storages = priorityEntry.getValue();
                    for (MEStorage storage : storages) {
                        if (remaining <= 0) {
                            break;
                        }
                        if (!(storage instanceof ConditionalPriorityStorage conditional)
                            || !conditional.sophisticatedAeAddons$isConditionActive()) {
                            if (!isQueuedForRemoval(storage)
                                && storage.isPreferredStorageFor(key, source)) {
                                long inserted = storage.insert(key, remaining, mode, source);
                                remaining -= inserted;
                            }
                        }
                    }
                    for (MEStorage storage : storages) {
                        if (remaining <= 0) {
                            break;
                        }
                        if (!(storage instanceof ConditionalPriorityStorage conditional)
                            || !conditional.sophisticatedAeAddons$isConditionActive()) {
                            if (!isQueuedForRemoval(storage)
                                && !storage.isPreferredStorageFor(key, source)) {
                                long inserted = storage.insert(key, remaining, mode, source);
                                remaining -= inserted;
                            }
                        }
                    }
                }
            }
            }
        } finally {
            if (migrationReentry) {
                MigrationRoutingContext.exitNetworkRouting();
            } else {
                mountsInUse = false;
                NetworkInsertionContext.end();
            }
        }

        if (!migrationReentry) {
            flushQueuedOperations();
        }
        cir.setReturnValue(preflightRejected ? 0L : amount - remaining);
    }
}
