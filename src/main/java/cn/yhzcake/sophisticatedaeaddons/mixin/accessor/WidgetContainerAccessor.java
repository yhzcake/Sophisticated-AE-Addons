package cn.yhzcake.sophisticatedaeaddons.mixin.accessor;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Consumer;

@Mixin(value = WidgetContainer.class, remap = false)
public interface WidgetContainerAccessor {
    @Invoker("populateScreen")
    void sophisticatedAeAddons$invokePopulateScreen(
        Consumer<AbstractWidget> addWidget,
        Rect2i bounds,
        AEBaseScreen<?> screen
    );

    @Invoker("drawBackgroundLayer")
    void sophisticatedAeAddons$invokeDrawBackgroundLayer(
        GuiGraphics graphics,
        Rect2i bounds,
        appeng.client.Point mousePoint
    );
}
