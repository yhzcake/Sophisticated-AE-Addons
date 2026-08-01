package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.inventory.AeUpgradeInventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StorageWrapper.class, remap = false)
public abstract class MixinStorageWrapper {
    @Inject(
        method = "getInventoryForUpgradeProcessing()Lnet/p3pp3rf1y/sophisticatedcore/inventory/ITrackedContentsItemHandler;",
        at = @At("RETURN"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$addAeInventory(
        CallbackInfoReturnable<ITrackedContentsItemHandler> cir
    ) {
        cir.setReturnValue(new AeUpgradeInventoryHandler((IStorageWrapper) this, cir.getReturnValue()));
    }
}
