package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.MEStorage;
import appeng.parts.storagebus.StorageBusPart;
import cn.yhzcake.sophisticatedaeaddons.ModDataComponents;
import cn.yhzcake.sophisticatedaeaddons.ModItems;
import cn.yhzcake.sophisticatedaeaddons.PriorityConditions;
import cn.yhzcake.sophisticatedaeaddons.priority.ConditionalPriorityStorage;
import cn.yhzcake.sophisticatedaeaddons.priority.MigrationRoutingContext;
import cn.yhzcake.sophisticatedaeaddons.priority.NetworkInsertionContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StorageBusPart.class, remap = false)
public abstract class MixinStorageBusPart {
    private boolean sophisticatedAeAddons$migrating;
    private AEKey sophisticatedAeAddons$blockedKey;

    @Redirect(
        method = "mountInventories(Lappeng/api/storage/IStorageMounts;)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/storage/IStorageMounts;mount(Lappeng/api/storage/MEStorage;I)V"
        ),
        require = 1
    )
    private void sophisticatedAeAddons$mountConditionalStorage(
        IStorageMounts mounts,
        MEStorage storage,
        int priority
    ) {
        PriorityConditions conditions = sophisticatedAeAddons$getConditions();
        if (conditions == null) {
            mounts.mount(storage, priority);
            return;
        }

        mounts.mount(new ConditionalStorage(storage), priority);
        mounts.mount(new ExtractionStorage(storage), priority);
    }

    private PriorityConditions sophisticatedAeAddons$getConditions() {
        StorageBusPart self = (StorageBusPart) (Object) this;
        for (int i = 0; i < self.getUpgrades().size(); i++) {
            ItemStack stack = self.getUpgrades().getStackInSlot(i);
            if (stack.is(ModItems.PRECISE_PRIORITY_CARD.get())) {
                return stack.getOrDefault(
                    ModDataComponents.PRIORITY_CONDITIONS,
                    PriorityConditions.EMPTY
                );
            }
        }
        return null;
    }

    private long sophisticatedAeAddons$getStoredAmount(MEStorage storage, AEKey key) {
        KeyCounter counter = new KeyCounter();
        storage.getAvailableStacks(counter);
        return counter.get(key);
    }

    private long sophisticatedAeAddons$getNetworkStoredAmount(AEKey key) {
        StorageBusPart self = (StorageBusPart) (Object) this;
        var grid = self.getMainNode().getGrid();
        return grid == null ? 0 : grid.getStorageService().getInventory().getAvailableStacks().get(key);
    }

    private long sophisticatedAeAddons$getConditionStoredAmount(
        MEStorage storage,
        PriorityConditions conditions,
        AEKey key
    ) {
        long localStored = sophisticatedAeAddons$getStoredAmount(storage, key);
        if (conditions.migrationMode() == PriorityConditions.MigrationMode.OFF) {
            return localStored;
        }
        long networkStored = NetworkInsertionContext.stored(
            key,
            sophisticatedAeAddons$getNetworkStoredAmount(key)
        );
        return Math.max(localStored, networkStored);
    }

    private long sophisticatedAeAddons$allowedAmount(
        MEStorage storage,
        PriorityConditions conditions,
        AEKey key,
        long requested,
        IActionSource source,
        long stored
    ) {
        if (requested <= 0) {
            return 0;
        }
        long physicalLimit = storage.insert(key, requested, Actionable.SIMULATE, source);
        if (physicalLimit <= 0) {
            return 0;
        }
        if (conditions.test(key, sophisticatedAeAddons$saturatedAdd(stored, physicalLimit))) {
            return physicalLimit;
        }
        long low = 0;
        long high = physicalLimit;
        while (low + 1 < high) {
            long middle = low + (high - low) / 2;
            if (conditions.test(key, sophisticatedAeAddons$saturatedAdd(stored, middle))) {
                low = middle;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private long sophisticatedAeAddons$allowedAmount(
        MEStorage storage,
        PriorityConditions conditions,
        AEKey key,
        long requested,
        IActionSource source
    ) {
        return sophisticatedAeAddons$allowedAmount(
            storage,
            conditions,
            key,
            requested,
            source,
            sophisticatedAeAddons$getStoredAmount(storage, key)
        );
    }

    private long sophisticatedAeAddons$saturatedAdd(long left, long right) {
        return right > 0 && left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private boolean sophisticatedAeAddons$canMigrate(
        MEStorage storage,
        AEKey key,
        IActionSource source
    ) {
        StorageBusPart self = (StorageBusPart) (Object) this;
        var grid = self.getMainNode().getGrid();
        if (grid == null) {
            return false;
        }
        long stored = sophisticatedAeAddons$getStoredAmount(storage, key);
        if (stored <= 0) {
            return true;
        }
        sophisticatedAeAddons$blockedKey = key;
        try {
            long extractable = storage.extract(key, stored, Actionable.SIMULATE, source);
            if (!MigrationRoutingContext.begin()) {
                return false;
            }
            long insertable;
            try {
                insertable = grid.getStorageService().getInventory().insert(key, extractable, Actionable.SIMULATE, source);
            } finally {
                MigrationRoutingContext.end();
            }
            return extractable == stored && insertable == stored;
        } finally {
            sophisticatedAeAddons$blockedKey = null;
        }
    }

    private boolean sophisticatedAeAddons$migrate(MEStorage storage, AEKey key, IActionSource source) {
        if (sophisticatedAeAddons$migrating || !sophisticatedAeAddons$canMigrate(storage, key, source)) {
            return false;
        }
        StorageBusPart self = (StorageBusPart) (Object) this;
        var grid = self.getMainNode().getGrid();
        long stored = sophisticatedAeAddons$getStoredAmount(storage, key);
        if (stored <= 0) {
            return true;
        }
        sophisticatedAeAddons$migrating = true;
        sophisticatedAeAddons$blockedKey = key;
        try {
            long extracted = storage.extract(key, stored, Actionable.MODULATE, source);
            if (extracted != stored) {
                sophisticatedAeAddons$restoreExtracted(storage, key, extracted, source);
                return false;
            }
            if (!MigrationRoutingContext.begin()) {
                sophisticatedAeAddons$restoreExtracted(storage, key, extracted, source);
                return false;
            }
            long inserted;
            try {
                inserted = grid.getStorageService().getInventory().insert(key, extracted, Actionable.MODULATE, source);
            } finally {
                MigrationRoutingContext.end();
            }
            if (inserted == extracted) {
                return true;
            }
            long retracted = inserted == 0
                ? 0
                : grid.getStorageService().getInventory().extract(key, inserted, Actionable.MODULATE, source);
            sophisticatedAeAddons$restoreExtracted(storage, key, extracted - inserted + retracted, source);
            return false;
        } finally {
            sophisticatedAeAddons$blockedKey = null;
            sophisticatedAeAddons$migrating = false;
        }
    }

    private void sophisticatedAeAddons$restoreExtracted(
        MEStorage storage,
        AEKey key,
        long amount,
        IActionSource source
    ) {
        if (amount <= 0) {
            return;
        }
        long restored = storage.insert(key, amount, Actionable.MODULATE, source);
        long remainder = amount - restored;
        if (remainder > 0) {
            StorageBusPart self = (StorageBusPart) (Object) this;
            var grid = self.getMainNode().getGrid();
            if (grid != null) {
                grid.getStorageService().getInventory().insert(key, remainder, Actionable.MODULATE, source);
            }
        }
    }

    private final class ConditionalStorage implements MEStorage, ConditionalPriorityStorage {
        private final MEStorage delegate;

        private ConditionalStorage(MEStorage delegate) {
            this.delegate = delegate;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (amount <= 0 || what.equals(sophisticatedAeAddons$blockedKey)) {
                return 0;
            }
            PriorityConditions conditions = sophisticatedAeAddons$getConditions();
            if (conditions == null) {
                return delegate.insert(what, amount, mode, source);
            }
            long stored = sophisticatedAeAddons$getConditionStoredAmount(delegate, conditions, what);
            long evaluatedAmount = conditions.migrationMode() == PriorityConditions.MigrationMode.OFF
                ? amount
                : NetworkInsertionContext.requested(what, amount);
            long allowed = sophisticatedAeAddons$allowedAmount(
                delegate,
                conditions,
                what,
                evaluatedAmount,
                source,
                stored
            );
            long physicalLimit = delegate.insert(what, evaluatedAmount, Actionable.SIMULATE, source);
            if (conditions.migrationMode() == PriorityConditions.MigrationMode.OFF) {
                return delegate.insert(what, allowed, mode, source);
            }
            boolean conditionMatches = conditions.test(
                what,
                sophisticatedAeAddons$saturatedAdd(stored, physicalLimit)
            );
            if (mode == Actionable.SIMULATE) {
                if (conditionMatches) {
                    return Math.min(amount, physicalLimit);
                }
                if (conditions.migrationMode() == PriorityConditions.MigrationMode.FORCE
                    && !sophisticatedAeAddons$canMigrate(delegate, what, source)) {
                    NetworkInsertionContext.blockFallback(what);
                }
                return 0;
            }
            if (conditionMatches) {
                return delegate.insert(what, amount, mode, source);
            }
            if (conditions.migrationMode() == PriorityConditions.MigrationMode.FORCE) {
                boolean migrated = sophisticatedAeAddons$migrate(delegate, what, source);
                if (!migrated) {
                    NetworkInsertionContext.blockFallback(what);
                }
            }
            return 0;
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            PriorityConditions conditions = sophisticatedAeAddons$getConditions();
            if (conditions == null) {
                return delegate.isPreferredStorageFor(what, source);
            }
            return sophisticatedAeAddons$allowedAmount(
                delegate,
                conditions,
                what,
                1,
                source,
                sophisticatedAeAddons$getConditionStoredAmount(delegate, conditions, what)
            ) > 0;
        }

        @Override
        public boolean sophisticatedAeAddons$isConditionActive() {
            return sophisticatedAeAddons$getConditions() != null;
        }

        @Override
        public boolean sophisticatedAeAddons$requiresNetworkSnapshot() {
            PriorityConditions conditions = sophisticatedAeAddons$getConditions();
            return conditions != null
                && conditions.migrationMode() != PriorityConditions.MigrationMode.OFF;
        }

        @Override
        public Component getDescription() {
            return delegate.getDescription();
        }
    }

    private final class ExtractionStorage implements MEStorage {
        private final MEStorage delegate;

        private ExtractionStorage(MEStorage delegate) {
            this.delegate = delegate;
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            return 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (what.equals(sophisticatedAeAddons$blockedKey)) {
                return 0;
            }
            return delegate.extract(what, amount, mode, source);
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            delegate.getAvailableStacks(out);
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return false;
        }

        @Override
        public Component getDescription() {
            return delegate.getDescription();
        }
    }
}
