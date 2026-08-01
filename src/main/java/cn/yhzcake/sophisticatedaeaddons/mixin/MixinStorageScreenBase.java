package cn.yhzcake.sophisticatedaeaddons.mixin;

import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolSlots;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import cn.yhzcake.sophisticatedaeaddons.inventory.SophisticatedToolItemHandler;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.AbstractContainerScreenAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StorageScreenBase.class, remap = false)
public abstract class MixinStorageScreenBase {
    @Shadow
    protected abstract void renderSlot(GuiGraphics graphics, Slot slot);

    @Shadow
    protected abstract boolean isHovering(Slot slot, double mouseX, double mouseY);

    @Inject(
        method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$drawToolPanel(
        GuiGraphics graphics,
        float partialTick,
        int mouseX,
        int mouseY,
        CallbackInfo ci
    ) {
        StorageScreenBase<?> self = (StorageScreenBase<?>) (Object) this;
        Slot firstToolSlot = SophisticatedToolSlots.positionNextToPlayerInventory(self.getMenu().slots);
        if (firstToolSlot == null) {
            return;
        }
        int x = self.getGuiLeft() + firstToolSlot.x - 1;
        int y = self.getGuiTop() + firstToolSlot.y - 1;
        graphics.fill(x - 3, y - 3, x + 57, y + 57, 0xFF2B2B2B);
        graphics.fill(x - 2, y - 2, x + 56, y + 56, 0xFFC6C6C6);
        for (int slot = 0; slot < 9; slot++) {
            int slotX = x + slot % 3 * 18;
            int slotY = y + slot / 3 * 18;
            graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
            graphics.fill(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);
        }
    }

    @SuppressWarnings("null")
    @Inject(
        method = "renderSuper(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            shift = At.Shift.BEFORE
        ),
        require = 1
    )
    private void sophisticatedAeAddons$renderToolSlots(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick,
        CallbackInfo ci
    ) {
        StorageScreenBase<?> self = (StorageScreenBase<?>) (Object) this;
        for (Slot slot : self.getMenu().slots) {
            if (!(slot instanceof SlotItemHandler itemHandlerSlot)
                || !(itemHandlerSlot.getItemHandler() instanceof SophisticatedToolItemHandler)) {
                continue;
            }
            if (slot.isActive()) {
                renderSlot(graphics, slot);
            }
            if (slot.isActive() && isHovering(slot, mouseX, mouseY)) {
                ((AbstractContainerScreenAccessor) self).sophisticatedAeAddons$setHoveredSlot(slot);
                AbstractContainerScreen.renderSlotHighlight(graphics, slot.x, slot.y, 0);
            }
        }
    }
}
