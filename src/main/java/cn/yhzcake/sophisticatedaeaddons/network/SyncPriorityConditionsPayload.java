package cn.yhzcake.sophisticatedaeaddons.network;

import cn.yhzcake.sophisticatedaeaddons.PriorityConditions;
import cn.yhzcake.sophisticatedaeaddons.SophisticatedAEAddons;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityMenuExtension;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@SuppressWarnings("null")
public record SyncPriorityConditionsPayload(
    int containerId,
    boolean hasCard,
    PriorityConditions conditions,
    int scrollOffset,
    int selectedIndex
) implements CustomPacketPayload {
    public static final Type<SyncPriorityConditionsPayload> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(SophisticatedAEAddons.MOD_ID, "sync_priority_conditions")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPriorityConditionsPayload> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncPriorityConditionsPayload::containerId,
            ByteBufCodecs.BOOL,
            SyncPriorityConditionsPayload::hasCard,
            PriorityConditions.STREAM_CODEC,
            SyncPriorityConditionsPayload::conditions,
            ByteBufCodecs.VAR_INT,
            SyncPriorityConditionsPayload::scrollOffset,
            ByteBufCodecs.VAR_INT,
            SyncPriorityConditionsPayload::selectedIndex,
            SyncPriorityConditionsPayload::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncPriorityConditionsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().containerMenu.containerId == payload.containerId()
                && context.player().containerMenu instanceof PrecisePriorityMenuExtension menu) {
                menu.sophisticatedAeAddons$applySync(
                    payload.hasCard(),
                    payload.conditions(),
                    payload.scrollOffset(),
                    payload.selectedIndex()
                );
            }
        });
    }
}
