package cn.yhzcake.sophisticatedaeaddons.priority;

import cn.yhzcake.sophisticatedaeaddons.PriorityConditions;
public interface PrecisePriorityMenuExtension {
    int VISIBLE_ROWS = 5;

    boolean sophisticatedAeAddons$hasCard();

    PriorityConditions sophisticatedAeAddons$getConditions();

    int sophisticatedAeAddons$getScrollOffset();

    int sophisticatedAeAddons$getSelectedIndex();

    void sophisticatedAeAddons$handleAction(Action action, int index, long value);

    void sophisticatedAeAddons$applySync(boolean hasCard, PriorityConditions conditions, int scrollOffset, int selectedIndex);

    void sophisticatedAeAddons$syncIfChanged();

    enum Action {
        SELECT,
        CLEAR_SELECTION,
        ADD,
        DELETE,
        CYCLE_LOGIC,
        TOGGLE_NEGATED,
        CYCLE_KEY_TYPE,
        CYCLE_TYPE,
        CYCLE_COMPARISON,
        SET_VALUE,
        SET_SCROLL_OFFSET,
        CYCLE_MIGRATION
    }
}
