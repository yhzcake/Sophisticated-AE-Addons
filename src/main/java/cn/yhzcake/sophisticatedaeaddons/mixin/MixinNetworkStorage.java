package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import appeng.me.storage.NetworkStorage;
import cn.yhzcake.sophisticatedaeaddons.priority.ConditionalPriorityStorage;
import cn.yhzcake.sophisticatedaeaddons.priority.MigrationRoutingContext;
import cn.yhzcake.sophisticatedaeaddons.priority.NetworkInsertionContext;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
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
    protected abstract void flushQueuedOperations();

    @Inject(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At("HEAD"),
        require = 1
    )
    private void sophisticatedAeAddons$beginInsertion(
        AEKey key,
        long amount,
        Actionable mode,
        IActionSource source,
        CallbackInfoReturnable<Long> cir
    ) {
        long stored = 0;
        boolean requiresSnapshot = priorityInventory.values().stream()
            .flatMap(List::stream)
            .filter(ConditionalPriorityStorage.class::isInstance)
            .map(ConditionalPriorityStorage.class::cast)
            .anyMatch(storage -> storage.sophisticatedAeAddons$isConditionActive()
                && storage.sophisticatedAeAddons$requiresNetworkSnapshot());
        if (requiresSnapshot) {
            stored = ((NetworkStorage) (Object) this).getAvailableStacks().get(key);
        }
        NetworkInsertionContext.begin(key, stored, amount);
    }

    @Inject(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At("RETURN"),
        require = 1
    )
    private void sophisticatedAeAddons$endInsertion(
        AEKey key,
        long amount,
        Actionable mode,
        IActionSource source,
        CallbackInfoReturnable<Long> cir
    ) {
        NetworkInsertionContext.end();
    }

    @ModifyExpressionValue(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At(
            value = "FIELD",
            target = "Lappeng/me/storage/NetworkStorage;mountsInUse:Z",
            opcode = org.objectweb.asm.Opcodes.GETFIELD
        ),
        require = 1
    )
    private boolean sophisticatedAeAddons$allowMigrationReentry(boolean mountsInUse) {
        if (mountsInUse && MigrationRoutingContext.enterNetworkRouting()) {
            return false;
        }
        return mountsInUse;
    }

    @Inject(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At("RETURN"),
        require = 1
    )
    private void sophisticatedAeAddons$exitMigrationReentry(
        AEKey key,
        long amount,
        Actionable mode,
        IActionSource source,
        CallbackInfoReturnable<Long> cir
    ) {
        if (MigrationRoutingContext.isNetworkRouting()) {
            MigrationRoutingContext.exitNetworkRouting();
        }
    }

    @Redirect(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At(
            value = "FIELD",
            target = "Lappeng/me/storage/NetworkStorage;mountsInUse:Z",
            opcode = org.objectweb.asm.Opcodes.PUTFIELD
        ),
        require = 3
    )
    private void sophisticatedAeAddons$preserveOuterMountState(NetworkStorage instance, boolean value) {
        if (!MigrationRoutingContext.isNetworkRouting()) {
            mountsInUse = value;
        }
    }

    @Redirect(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/me/storage/NetworkStorage;flushQueuedOperations()V"
        ),
        require = 1
    )
    private void sophisticatedAeAddons$deferMigrationFlush(NetworkStorage instance) {
        if (!MigrationRoutingContext.isNetworkRouting()) {
            flushQueuedOperations();
        }
    }

    @ModifyExpressionValue(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/NavigableMap;values()Ljava/util/Collection;"
        ),
        require = 1
    )
    private Collection<List<MEStorage>> sophisticatedAeAddons$conditionFirst(
        Collection<List<MEStorage>> inventories
    ) {
        List<List<MEStorage>> conditional = new ArrayList<>();
        List<List<MEStorage>> ordinary = new ArrayList<>();
        for (List<MEStorage> inventory : inventories) {
            List<MEStorage> conditionalStorages = new ArrayList<>();
            List<MEStorage> ordinaryStorages = new ArrayList<>();
            for (MEStorage storage : inventory) {
                if (storage instanceof ConditionalPriorityStorage condition
                    && condition.sophisticatedAeAddons$isConditionActive()) {
                    conditionalStorages.add(storage);
                } else {
                    ordinaryStorages.add(storage);
                }
            }
            if (!conditionalStorages.isEmpty()) {
                conditional.add(conditionalStorages);
            }
            if (!ordinaryStorages.isEmpty()) {
                ordinary.add(ordinaryStorages);
            }
        }
        Collection<List<MEStorage>> result = new ArrayList<>();
        result.addAll(conditional);
        result.addAll(ordinary);
        return result;
    }

    @WrapOperation(
        method = "insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/storage/MEStorage;insert(Lappeng/api/stacks/AEKey;JLappeng/api/config/Actionable;Lappeng/api/networking/security/IActionSource;)J"
        ),
        require = 1
    )
    private long sophisticatedAeAddons$wrapStorageInsert(
        MEStorage storage,
        AEKey key,
        long amount,
        Actionable mode,
        IActionSource source,
        Operation<Long> original
    ) {
        if (NetworkInsertionContext.isFallbackBlocked(key)
            && !(storage instanceof ConditionalPriorityStorage)) {
            return 0;
        }
        return original.call(storage, key, amount, mode, source);
    }
}
