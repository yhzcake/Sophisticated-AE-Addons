package cn.yhzcake.sophisticatedaeaddons.priority;

import net.minecraft.client.gui.GuiGraphics;

public interface PrecisePriorityScreenExtension {
    void sophisticatedAeAddons$prepareLayout();

    boolean sophisticatedAeAddons$isExpanded();

    void sophisticatedAeAddons$initializePanel();

    void sophisticatedAeAddons$refreshPanel();

    void sophisticatedAeAddons$drawPanel(GuiGraphics graphics, int offsetX, int offsetY);

    boolean sophisticatedAeAddons$mouseScrolled(double mouseX, double mouseY, double scrollY);

    boolean sophisticatedAeAddons$mouseClicked(double mouseX, double mouseY, int button);
}
