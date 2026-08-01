package cn.yhzcake.sophisticatedaeaddons.mixin.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("hoveredSlot")
    void sophisticatedAeAddons$setHoveredSlot(Slot slot);

    @Accessor("imageWidth")
    void sophisticatedAeAddons$setImageWidth(int imageWidth);

    @Accessor("imageHeight")
    void sophisticatedAeAddons$setImageHeight(int imageHeight);
}
