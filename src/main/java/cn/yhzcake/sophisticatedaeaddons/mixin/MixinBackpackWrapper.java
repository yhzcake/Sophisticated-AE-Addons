package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.inventory.AeUpgradeInventoryHandler;
import cn.yhzcake.sophisticatedaeaddons.compat.BackpackWrapperIndex;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackpackWrapper.class, remap = false)
public abstract class MixinBackpackWrapper {
    @Inject(
        method = "fromStack(Lnet/minecraft/world/item/ItemStack;)Lnet/p3pp3rf1y/sophisticatedbackpacks/backpack/wrapper/IBackpackWrapper;",
        at = @At("RETURN"),
        require = 1
    )
    private static void sophisticatedAeAddons$indexWrapper(
        ItemStack stack,
        CallbackInfoReturnable<IBackpackWrapper> cir
    ) {
        BackpackWrapperIndex.register(cir.getReturnValue());
    }

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
