package cn.yhzcake.sophisticatedaeaddons.mixin.accessor;

import net.minecraft.world.inventory.Slot;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = StorageContainerMenuBase.class, remap = false)
public interface StorageContainerMenuBaseAccessor {
    @Invoker("addExtraSlot")
    void sophisticatedAeAddons$invokeAddExtraSlot(Slot slot);
}
