package cn.yhzcake.sophisticatedaeaddons.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;

public record PriorityConditionRowWidgets(
    Button logic,
    Button negated,
    Button keyType,
    Button type,
    Button comparison,
    EditBox value,
    Button confirm,
    Button delete
) {
    public void setVisible(boolean visible) {
        logic.visible = visible;
        negated.visible = visible;
        keyType.visible = visible;
        type.visible = visible;
        comparison.visible = visible;
        value.visible = visible;
        confirm.visible = visible;
        delete.visible = visible;
    }

    public void setActive(boolean active) {
        logic.active = active;
        negated.active = active;
        keyType.active = active;
        type.active = active;
        comparison.active = active;
        value.active = active;
        confirm.active = active;
        delete.active = active;
    }
}
