package cn.yhzcake.sophisticatedaeaddons;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@SuppressWarnings("null")
public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SophisticatedAEAddons.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UpgradeToolContents>> UPGRADE_TOOL_CONTENTS =
        DATA_COMPONENTS.register("upgrade_tool_contents",
            () -> DataComponentType.<UpgradeToolContents>builder()
                .persistent(UpgradeToolContents.CODEC)
                .networkSynchronized(UpgradeToolContents.STREAM_CODEC)
                .build());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PriorityConditions>> PRIORITY_CONDITIONS =
        DATA_COMPONENTS.register("priority_conditions",
            () -> DataComponentType.<PriorityConditions>builder()
                .persistent(PriorityConditions.CODEC)
                .networkSynchronized(PriorityConditions.STREAM_CODEC)
                .build());

    private ModDataComponents() {
    }

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
