package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.stacks.AEKeyType;
import appeng.client.gui.implementations.PriorityScreen;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.AbstractContainerScreenAccessor;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.AEBaseScreenAccessor;
import cn.yhzcake.sophisticatedaeaddons.mixin.accessor.ScreenAccessor;
import cn.yhzcake.sophisticatedaeaddons.network.SetPriorityConditionValuePayload;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityMenuExtension;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityScreenExtension;
import cn.yhzcake.sophisticatedaeaddons.priority.PriorityCondition;
import cn.yhzcake.sophisticatedaeaddons.screen.PriorityConditionRowWidgets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("null")
@Mixin(value = PriorityScreen.class, remap = false)
public abstract class MixinPriorityScreen implements PrecisePriorityScreenExtension {
    @Unique
    private static final int sophisticatedAeAddons$expandedWidth = 310;
    @Unique
    private static final int sophisticatedAeAddons$expandedHeight = 270;
    @Unique
    private static final int sophisticatedAeAddons$nativeWidth = 176;
    @Unique
    private static final int sophisticatedAeAddons$nativeHeight = 125;
    @Unique
    private static final int sophisticatedAeAddons$nativeOffsetX = (sophisticatedAeAddons$expandedWidth - sophisticatedAeAddons$nativeWidth) / 2;
    @Unique
    private static final int sophisticatedAeAddons$conditionsTop = 130;
    @Unique
    private final List<PriorityConditionRowWidgets> sophisticatedAeAddons$rows = new ArrayList<>();
    @Unique
    private Button sophisticatedAeAddons$add;
    @Unique
    private Button sophisticatedAeAddons$migration;
    @Unique
    private boolean sophisticatedAeAddons$expanded;
    @Unique
    private int sophisticatedAeAddons$displayedOffset = -1;
    @Unique
    private int sophisticatedAeAddons$pendingOffset;

    @Override
    public void sophisticatedAeAddons$prepareLayout() {
        PriorityScreen self = (PriorityScreen) (Object) this;
        boolean expanded = sophisticatedAeAddons$menu().sophisticatedAeAddons$hasCard();
        sophisticatedAeAddons$expanded = expanded;
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) self;
        accessor.sophisticatedAeAddons$setImageWidth(expanded ? sophisticatedAeAddons$expandedWidth : 176);
        accessor.sophisticatedAeAddons$setImageHeight(expanded ? sophisticatedAeAddons$expandedHeight : 125);
        AEBaseScreenAccessor screenAccessor = (AEBaseScreenAccessor) self;
        screenAccessor.sophisticatedAeAddons$invokeSetTextHidden("priority_insertion_hint", expanded);
        screenAccessor.sophisticatedAeAddons$invokeSetTextHidden("priority_extraction_hint", expanded);
        screenAccessor.sophisticatedAeAddons$invokeSetTextHidden("dialog_title", expanded);
    }

    @Override
    public boolean sophisticatedAeAddons$isExpanded() {
        return sophisticatedAeAddons$expanded;
    }

    @Override
    public void sophisticatedAeAddons$initializePanel() {
        sophisticatedAeAddons$rows.clear();
        if (!sophisticatedAeAddons$expanded) {
            return;
        }
        PriorityScreen self = (PriorityScreen) (Object) this;
        ScreenAccessor accessor = (ScreenAccessor) self;
        int left = self.getGuiLeft();
        int top = self.getGuiTop();
        for (int row = 0; row < PrecisePriorityMenuExtension.VISIBLE_ROWS; row++) {
            int y = top + sophisticatedAeAddons$conditionsTop + 18 + row * 20;
            int rowIndex = row;
            Button logic = Button.builder(Component.literal("OR"), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_LOGIC, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 8, y, 34, 18).build();
            Button negated = Button.builder(Component.translatable("gui.sophisticated_ae_addons.not"), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.TOGGLE_NEGATED, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 46, y, 34, 18).build();
            Button keyType = Button.builder(Component.empty(), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_KEY_TYPE, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 84, y, 44, 18).build();
            Button type = Button.builder(Component.translatable("gui.sophisticated_ae_addons.count"), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_TYPE, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 132, y, 38, 18).build();
            Button comparison = Button.builder(Component.literal("="), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_COMPARISON, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 174, y, 26, 18).build();
            EditBox value = new EditBox(self.getMinecraft().font, left + 204, y, 42, 18, Component.literal("value"));
            value.setMaxLength(20);
            value.setFilter(text -> text.isEmpty() || text.equals("-") || text.matches("-?[0-9]+"));
            Button confirm = Button.builder(Component.literal("✓"), button ->
                sophisticatedAeAddons$submitValue(rowIndex, value)
            ).bounds(left + 250, y, 18, 18).build();
            Button delete = Button.builder(Component.literal("×"), button ->
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.DELETE, sophisticatedAeAddons$absoluteIndex(rowIndex), 0)
            ).bounds(left + 272, y, 18, 18).build();
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(logic);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(negated);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(keyType);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(type);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(comparison);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(value);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(confirm);
            accessor.sophisticatedAeAddons$invokeAddRenderableWidget(delete);
            sophisticatedAeAddons$rows.add(new PriorityConditionRowWidgets(
                logic, negated, keyType, type, comparison, value, confirm, delete
            ));
        }
        sophisticatedAeAddons$add = Button.builder(Component.literal("+"), button ->
            sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.ADD, sophisticatedAeAddons$menu().sophisticatedAeAddons$getSelectedIndex(), 0)
        ).bounds(left + 8, top + 250, 136, 18).build();
        sophisticatedAeAddons$migration = Button.builder(Component.empty(), button ->
            sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_MIGRATION, -1, 0)
        ).bounds(left + 154, top + 250, 136, 18).build();
        accessor.sophisticatedAeAddons$invokeAddRenderableWidget(sophisticatedAeAddons$add);
        accessor.sophisticatedAeAddons$invokeAddRenderableWidget(sophisticatedAeAddons$migration);
        sophisticatedAeAddons$displayedOffset = -1;
        sophisticatedAeAddons$pendingOffset = sophisticatedAeAddons$menu().sophisticatedAeAddons$getScrollOffset();
        sophisticatedAeAddons$refresh();
    }

    @Override
    public void sophisticatedAeAddons$refreshPanel() {
        PriorityScreen self = (PriorityScreen) (Object) this;
        boolean shouldExpand = sophisticatedAeAddons$menu().sophisticatedAeAddons$hasCard();
        if (shouldExpand != sophisticatedAeAddons$expanded) {
            self.getMinecraft().setScreen(self);
            return;
        }
        sophisticatedAeAddons$refresh();
    }

    @Override
    public void sophisticatedAeAddons$drawPanel(GuiGraphics graphics, int offsetX, int offsetY) {
        if (!sophisticatedAeAddons$expanded) {
            return;
        }
        ((AEBaseScreenAccessor) (PriorityScreen) (Object) this).sophisticatedAeAddons$getStyle()
            .getBackground()
            .dest(offsetX + sophisticatedAeAddons$nativeOffsetX, offsetY)
            .blit(graphics);
        int panelTop = offsetY + sophisticatedAeAddons$conditionsTop - 2;
        int panelBottom = offsetY + sophisticatedAeAddons$expandedHeight;
        graphics.fill(offsetX, panelTop, offsetX + sophisticatedAeAddons$expandedWidth, panelBottom, 0xFF202020);
        graphics.fill(offsetX + 1, panelTop + 1, offsetX + sophisticatedAeAddons$expandedWidth - 1, panelBottom - 1, 0xFFC6C6C6);
        graphics.drawString(
            ((PriorityScreen) (Object) this).getMinecraft().font,
            Component.translatable("gui.ae2.Priority"),
            offsetX + sophisticatedAeAddons$nativeOffsetX + 8,
            offsetY + 6,
            0xFF303030,
            false
        );
        graphics.drawString(
            ((PriorityScreen) (Object) this).getMinecraft().font,
            Component.translatable("gui.sophisticated_ae_addons.priority_conditions"),
            offsetX + 8,
            offsetY + sophisticatedAeAddons$conditionsTop + 4,
            0xFF303030,
            false
        );
        int visible = Math.min(
            PrecisePriorityMenuExtension.VISIBLE_ROWS,
            sophisticatedAeAddons$menu().sophisticatedAeAddons$getConditions().conditions().size()
                - sophisticatedAeAddons$menu().sophisticatedAeAddons$getScrollOffset()
        );
        for (int row = 0; row < visible; row++) {
            int y = offsetY + sophisticatedAeAddons$conditionsTop + 17 + row * 20;
            boolean selected = sophisticatedAeAddons$absoluteIndex(row) == sophisticatedAeAddons$menu().sophisticatedAeAddons$getSelectedIndex();
            if (selected) {
                graphics.fill(offsetX + 5, y - 1, offsetX + 292, y + 19, 0xFF6B6B94);
            }
        }
        sophisticatedAeAddons$drawScrollbar(graphics, offsetX, offsetY);
    }

    @Override
    public boolean sophisticatedAeAddons$mouseScrolled(double mouseX, double mouseY, double scrollY) {
        if (!sophisticatedAeAddons$expanded || !sophisticatedAeAddons$insideConditions(mouseX, mouseY)) {
            return false;
        }
        PrecisePriorityMenuExtension menu = sophisticatedAeAddons$menu();
        int delta = scrollY < 0 ? 1 : scrollY > 0 ? -1 : 0;
        if (delta != 0) {
            int max = Math.max(0, menu.sophisticatedAeAddons$getConditions().conditions().size()
                - PrecisePriorityMenuExtension.VISIBLE_ROWS);
            sophisticatedAeAddons$pendingOffset = Math.max(0, Math.min(sophisticatedAeAddons$pendingOffset + delta, max));
            sophisticatedAeAddons$send(
                PrecisePriorityMenuExtension.Action.SET_SCROLL_OFFSET,
                -1,
                sophisticatedAeAddons$pendingOffset
            );
        }
        return true;
    }

    @Override
    public boolean sophisticatedAeAddons$mouseClicked(double mouseX, double mouseY, int button) {
        if (!sophisticatedAeAddons$expanded) {
            return false;
        }
        if (button == 1) {
            return sophisticatedAeAddons$handleRightClick(mouseX, mouseY);
        }
        if (button != 0) {
            return false;
        }
        PriorityScreen self = (PriorityScreen) (Object) this;
        int scrollbarX = self.getGuiLeft() + 298;
        int scrollbarTop = self.getGuiTop() + sophisticatedAeAddons$conditionsTop + 18;
        if (mouseX >= scrollbarX && mouseX < scrollbarX + 8 && mouseY >= scrollbarTop && mouseY < scrollbarTop + 106) {
            int size = sophisticatedAeAddons$menu().sophisticatedAeAddons$getConditions().conditions().size();
            int max = Math.max(0, size - PrecisePriorityMenuExtension.VISIBLE_ROWS);
            int offset = max == 0 ? 0 : (int) ((mouseY - scrollbarTop) * max / 105.0);
            sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.SET_SCROLL_OFFSET, -1, offset);
            return true;
        }
        int rowsTop = self.getGuiTop() + sophisticatedAeAddons$conditionsTop + 18;
        if (mouseX >= self.getGuiLeft() + 8
            && mouseX < self.getGuiLeft() + 292
            && mouseY >= rowsTop
            && mouseY < rowsTop + PrecisePriorityMenuExtension.VISIBLE_ROWS * 20) {
            int row = (int) ((mouseY - rowsTop) / 20);
            int index = sophisticatedAeAddons$absoluteIndex(row);
            if (index < sophisticatedAeAddons$menu().sophisticatedAeAddons$getConditions().conditions().size()) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.SELECT, index, 0);
            }
            return false;
        }
        if (sophisticatedAeAddons$add != null
            && (sophisticatedAeAddons$add.isMouseOver(mouseX, mouseY)
                || sophisticatedAeAddons$migration.isMouseOver(mouseX, mouseY))) {
            return false;
        }
        if (sophisticatedAeAddons$insideConditions(mouseX, mouseY)) {
            sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CLEAR_SELECTION, -1, 0);
        }
        return false;
    }

    @Unique
    private void sophisticatedAeAddons$refresh() {
        if (!sophisticatedAeAddons$expanded || sophisticatedAeAddons$rows.isEmpty()) {
            return;
        }
        PrecisePriorityMenuExtension menu = sophisticatedAeAddons$menu();
        List<PriorityCondition> conditions = menu.sophisticatedAeAddons$getConditions().conditions();
        int offset = menu.sophisticatedAeAddons$getScrollOffset();
        if (offset != sophisticatedAeAddons$displayedOffset) {
            for (PriorityConditionRowWidgets widgets : sophisticatedAeAddons$rows) {
                widgets.value().setFocused(false);
            }
            sophisticatedAeAddons$displayedOffset = offset;
            sophisticatedAeAddons$pendingOffset = offset;
        }
        for (int row = 0; row < sophisticatedAeAddons$rows.size(); row++) {
            int index = offset + row;
            PriorityConditionRowWidgets widgets = sophisticatedAeAddons$rows.get(row);
            boolean exists = index < conditions.size();
            widgets.setVisible(exists);
            widgets.setActive(exists);
            if (!exists) {
                continue;
            }
            PriorityCondition condition = conditions.get(index);
            widgets.logic().setMessage(Component.literal(condition.logicOp().name()));
            widgets.negated().setMessage(condition.negated()
                ? Component.translatable("gui.sophisticated_ae_addons.negated")
                : Component.translatable("gui.sophisticated_ae_addons.not_negated"));
            widgets.keyType().setMessage(condition.keyType().getDescription());
            widgets.type().setMessage(Component.translatable(condition.comparisonType() == PriorityCondition.ComparisonType.STACK
                ? "gui.sophisticated_ae_addons.stack"
                : "gui.sophisticated_ae_addons.count"));
            widgets.type().active = condition.keyType() == AEKeyType.items();
            widgets.comparison().setMessage(Component.literal(condition.comparisonOp().getSymbol()));
            if (!widgets.value().isFocused()) {
                widgets.value().setValue(Long.toString(condition.value()));
            }
        }
        sophisticatedAeAddons$migration.setMessage(Component.translatable(
            switch (menu.sophisticatedAeAddons$getConditions().migrationMode()) {
                case OFF -> "gui.sophisticated_ae_addons.migration.off";
                case ON -> "gui.sophisticated_ae_addons.migration.on";
                case FORCE -> "gui.sophisticated_ae_addons.migration.force";
            }
        ));
    }

    @Unique
    private void sophisticatedAeAddons$drawScrollbar(GuiGraphics graphics, int offsetX, int offsetY) {
        int size = sophisticatedAeAddons$menu().sophisticatedAeAddons$getConditions().conditions().size();
        if (size <= PrecisePriorityMenuExtension.VISIBLE_ROWS) {
            return;
        }
        int x = offsetX + 298;
        int y = offsetY + sophisticatedAeAddons$conditionsTop + 18;
        int height = 106;
        int max = size - PrecisePriorityMenuExtension.VISIBLE_ROWS;
        int handleHeight = Math.max(12, height * PrecisePriorityMenuExtension.VISIBLE_ROWS / size);
        int handleY = y + (height - handleHeight) * sophisticatedAeAddons$menu().sophisticatedAeAddons$getScrollOffset() / max;
        graphics.fill(x, y, x + 8, y + height, 0xFF555555);
        graphics.fill(x + 1, handleY, x + 7, handleY + handleHeight, 0xFFB8B8B8);
    }

    @Unique
    private boolean sophisticatedAeAddons$insideConditions(double mouseX, double mouseY) {
        PriorityScreen self = (PriorityScreen) (Object) this;
        return mouseX >= self.getGuiLeft()
            && mouseX < self.getGuiLeft() + sophisticatedAeAddons$expandedWidth
            && mouseY >= self.getGuiTop() + sophisticatedAeAddons$conditionsTop
            && mouseY < self.getGuiTop() + sophisticatedAeAddons$expandedHeight;
    }

    @Unique
    private int sophisticatedAeAddons$absoluteIndex(int row) {
        return sophisticatedAeAddons$menu().sophisticatedAeAddons$getScrollOffset() + row;
    }

    @Unique
    private boolean sophisticatedAeAddons$isRowControl(double mouseX, double mouseY, int row) {
        PriorityConditionRowWidgets widgets = sophisticatedAeAddons$rows.get(row);
        return widgets.logic().isMouseOver(mouseX, mouseY)
            || widgets.negated().isMouseOver(mouseX, mouseY)
            || widgets.keyType().isMouseOver(mouseX, mouseY)
            || widgets.type().isMouseOver(mouseX, mouseY)
            || widgets.comparison().isMouseOver(mouseX, mouseY)
            || widgets.value().isMouseOver(mouseX, mouseY)
            || widgets.confirm().isMouseOver(mouseX, mouseY)
            || widgets.delete().isMouseOver(mouseX, mouseY);
    }

    @Unique
    private boolean sophisticatedAeAddons$handleRightClick(double mouseX, double mouseY) {
        for (int row = 0; row < sophisticatedAeAddons$rows.size(); row++) {
            PriorityConditionRowWidgets widgets = sophisticatedAeAddons$rows.get(row);
            int index = sophisticatedAeAddons$absoluteIndex(row);
            if (index >= sophisticatedAeAddons$menu().sophisticatedAeAddons$getConditions().conditions().size()) {
                continue;
            }
            if (widgets.logic().isMouseOver(mouseX, mouseY)) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_LOGIC, index, -1);
                return true;
            }
            if (widgets.keyType().isMouseOver(mouseX, mouseY)) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_KEY_TYPE, index, -1);
                return true;
            }
            if (widgets.type().isMouseOver(mouseX, mouseY)) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_TYPE, index, -1);
                return true;
            }
            if (widgets.comparison().isMouseOver(mouseX, mouseY)) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.CYCLE_COMPARISON, index, -1);
                return true;
            }
            if (widgets.value().isMouseOver(mouseX, mouseY)) {
                sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action.SELECT, index, 0);
                widgets.value().setValue("");
                ((PriorityScreen) (Object) this).setFocused(widgets.value());
                return true;
            }
        }
        return false;
    }

    @Unique
    private void sophisticatedAeAddons$submitValue(int row, EditBox value) {
        try {
            sophisticatedAeAddons$send(
                PrecisePriorityMenuExtension.Action.SET_VALUE,
                sophisticatedAeAddons$absoluteIndex(row),
                Long.parseLong(value.getValue())
            );
        } catch (NumberFormatException ignored) {
        }
    }

    @Unique
    private void sophisticatedAeAddons$send(PrecisePriorityMenuExtension.Action action, int index, long value) {
        PriorityScreen self = (PriorityScreen) (Object) this;
        PacketDistributor.sendToServer(new SetPriorityConditionValuePayload(self.getMenu().containerId, action, index, value));
    }

    @Unique
    private PrecisePriorityMenuExtension sophisticatedAeAddons$menu() {
        return (PrecisePriorityMenuExtension) ((PriorityScreen) (Object) this).getMenu();
    }
}
