package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.compat.BackpackWrapperIndex;
import cn.yhzcake.sophisticatedaeaddons.upgrade.AeNetworkAccess;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InventoryHandler.class, remap = false)
public abstract class MixinInventoryHandler {
    @Shadow
    @Final
    protected IStorageWrapper storageWrapper;

    @Inject(
        method = "onContentsChanged(I)V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$synchronizeBackpackWrappers(int slot, CallbackInfo ci) {
        if (storageWrapper instanceof IBackpackWrapper backpackWrapper) {
            BackpackWrapperIndex.register(backpackWrapper);
            BackpackWrapperIndex.synchronizePeers(backpackWrapper);
        }
    }

    @Inject(
        method = "extractItem(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$extractFromAe(
        ItemStack requested,
        boolean simulate,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        cir.setReturnValue(AeNetworkAccess.extractMissing(
            storageWrapper,
            requested,
            cir.getReturnValue(),
            simulate
        ));
    }
}
