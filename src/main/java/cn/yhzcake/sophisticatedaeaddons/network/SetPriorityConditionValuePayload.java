package cn.yhzcake.sophisticatedaeaddons.network;

import cn.yhzcake.sophisticatedaeaddons.SophisticatedAEAddons;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityMenuExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetPriorityConditionValuePayload(
    int containerId,
    PrecisePriorityMenuExtension.Action action,
    int index,
    long value
) implements CustomPacketPayload {
    public static final Type<SetPriorityConditionValuePayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SophisticatedAEAddons.MOD_ID, "set_priority_condition_value")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetPriorityConditionValuePayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SetPriorityConditionValuePayload::containerId,
            ByteBufCodecs.VAR_INT.map(
                ordinal -> PrecisePriorityMenuExtension.Action.values()[ordinal],
                PrecisePriorityMenuExtension.Action::ordinal
            ),
            SetPriorityConditionValuePayload::action,
            ByteBufCodecs.VAR_INT,
            SetPriorityConditionValuePayload::index,
            ByteBufCodecs.VAR_LONG,
            SetPriorityConditionValuePayload::value,
            SetPriorityConditionValuePayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetPriorityConditionValuePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu.containerId == payload.containerId()
                && context.player().containerMenu instanceof PrecisePriorityMenuExtension menu) {
                menu.sophisticatedAeAddons$handleAction(payload.action(), payload.index(), payload.value());
            }
        });
    }
}
