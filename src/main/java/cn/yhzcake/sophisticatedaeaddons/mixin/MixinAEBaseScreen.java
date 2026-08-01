package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.implementations.PriorityScreen;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.WidgetContainerAccessor;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityScreenExtension;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AEBaseScreen.class, remap = false)
public abstract class MixinAEBaseScreen {
    @Redirect(
        method = "init()V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;populateScreen(Ljava/util/function/Consumer;Lnet/minecraft/client/renderer/Rect2i;Lappeng/client/gui/AEBaseScreen;)V"
        ),
        require = 1
    )
    private void sophisticatedAeAddons$centerPriorityWidgets(
        WidgetContainer widgets,
        java.util.function.Consumer<net.minecraft.client.gui.components.AbstractWidget> addWidget,
        Rect2i bounds,
        AEBaseScreen<?> screen
    ) {
        if ((Object) screen instanceof PriorityScreen priorityScreen
            && priorityScreen instanceof PrecisePriorityScreenExtension extension
            && extension.sophisticatedAeAddons$isExpanded()) {
            ((WidgetContainerAccessor) widgets).sophisticatedAeAddons$invokePopulateScreen(
                addWidget,
                new Rect2i(bounds.getX() + 67, bounds.getY(), 176, 125),
                screen
            );
            return;
        }
        ((WidgetContainerAccessor) widgets).sophisticatedAeAddons$invokePopulateScreen(addWidget, bounds, screen);
    }

    @Inject(method = "init()V", at = @At("HEAD"), require = 1)
    private void sophisticatedAeAddons$preparePriorityLayout(CallbackInfo ci) {
        if (this instanceof PrecisePriorityScreenExtension extension) {
            extension.sophisticatedAeAddons$prepareLayout();
        }
    }

    @Inject(method = "init()V", at = @At("TAIL"), require = 1)
    private void sophisticatedAeAddons$initializePriorityPanel(CallbackInfo ci) {
        if (this instanceof PrecisePriorityScreenExtension extension) {
            extension.sophisticatedAeAddons$initializePanel();
        }
    }

    @Inject(method = "updateBeforeRender()V", at = @At("TAIL"), require = 1)
    private void sophisticatedAeAddons$refreshPriorityPanel(CallbackInfo ci) {
        if (this instanceof PrecisePriorityScreenExtension extension) {
            extension.sophisticatedAeAddons$refreshPanel();
        }
    }

    @Inject(
        method = "drawBG(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$drawPriorityPanel(
        GuiGraphics graphics,
        int offsetX,
        int offsetY,
        int mouseX,
        int mouseY,
        float partialTicks,
        CallbackInfo ci
    ) {
        if (this instanceof PrecisePriorityScreenExtension extension && extension.sophisticatedAeAddons$isExpanded()) {
            extension.sophisticatedAeAddons$drawPanel(graphics, offsetX, offsetY);
            ci.cancel();
        }
    }

    @Redirect(
        method = "renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;drawBackgroundLayer(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/renderer/Rect2i;Lappeng/client/Point;)V"
        ),
        require = 1
    )
    private void sophisticatedAeAddons$centerPriorityToolbarBackground(
        WidgetContainer widgets,
        GuiGraphics graphics,
        Rect2i bounds,
        appeng.client.Point mousePoint
    ) {
        if ((Object) this instanceof PriorityScreen priorityScreen
            && priorityScreen instanceof PrecisePriorityScreenExtension extension
            && extension.sophisticatedAeAddons$isExpanded()) {
            ((WidgetContainerAccessor) widgets).sophisticatedAeAddons$invokeDrawBackgroundLayer(
                graphics,
                new Rect2i(bounds.getX() + 67, bounds.getY(), 176, 125),
                mousePoint
            );
            return;
        }
        ((WidgetContainerAccessor) widgets).sophisticatedAeAddons$invokeDrawBackgroundLayer(graphics, bounds, mousePoint);
    }

    @Inject(
        method = "mouseScrolled(DDDD)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$scrollPriorityPanel(
        double mouseX,
        double mouseY,
        double scrollX,
        double scrollY,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (this instanceof PrecisePriorityScreenExtension extension
            && extension.sophisticatedAeAddons$mouseScrolled(mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
        method = "mouseClicked(DDI)Z",
        at = @At("HEAD"),
        cancellable = true,
        require = 1
    )
    private void sophisticatedAeAddons$clickPriorityPanel(
        double mouseX,
        double mouseY,
        int button,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (this instanceof PrecisePriorityScreenExtension extension
            && extension.sophisticatedAeAddons$mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}
