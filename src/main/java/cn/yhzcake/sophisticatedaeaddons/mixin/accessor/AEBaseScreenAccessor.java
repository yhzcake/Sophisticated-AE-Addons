package cn.yhzcake.sophisticatedaeaddons.mixin.accessor;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseScreen.class, remap = false)
public interface AEBaseScreenAccessor {
    @Invoker("setTextHidden")
    void sophisticatedAeAddons$invokeSetTextHidden(String id, boolean hidden);

    @Accessor("style")
    ScreenStyle sophisticatedAeAddons$getStyle();
}
