package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.client.gui.me.common.MEStorageScreen;
import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolSlots;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MEStorageScreen.class, remap = false)
public abstract class MixinMEStorageScreen {
    @Inject(
        method = "drawBG(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$drawToolPanel(
        GuiGraphics graphics,
        int offsetX,
        int offsetY,
        int mouseX,
        int mouseY,
        float partialTicks,
        CallbackInfo ci
    ) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Slot firstToolSlot = SophisticatedToolSlots.positionNextToPlayerInventory(self.getMenu().slots);
        if (firstToolSlot == null) {
            return;
        }
        sophisticatedAeAddons$drawPanel(graphics, self.getGuiLeft(), self.getGuiTop(), firstToolSlot);
    }

    private void sophisticatedAeAddons$drawPanel(GuiGraphics graphics, int guiLeft, int guiTop, Slot firstToolSlot) {
        int x = guiLeft + firstToolSlot.x - 1;
        int y = guiTop + firstToolSlot.y - 1;
        graphics.fill(x - 3, y - 3, x + 57, y + 57, 0xFF2B2B2B);
        graphics.fill(x - 2, y - 2, x + 56, y + 56, 0xFFC6C6C6);
        for (int slot = 0; slot < 9; slot++) {
            int slotX = x + slot % 3 * 18;
            int slotY = y + slot / 3 * 18;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
            graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);
        }
    }
}
