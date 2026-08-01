package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityMenuExtension;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinAbstractContainerMenu {
    @Inject(
        method = "broadcastChanges()V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$syncPriorityConditions(CallbackInfo ci) {
        if (this instanceof PrecisePriorityMenuExtension extension) {
            extension.sophisticatedAeAddons$syncIfChanged();
        }
    }
}
