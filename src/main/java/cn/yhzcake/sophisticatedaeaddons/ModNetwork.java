package cn.yhzcake.sophisticatedaeaddons;

import cn.yhzcake.sophisticatedaeaddons.network.SetPriorityConditionValuePayload;
import cn.yhzcake.sophisticatedaeaddons.network.SyncPriorityConditionsPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@SuppressWarnings("null")
public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToServer(
            SetPriorityConditionValuePayload.TYPE,
            SetPriorityConditionValuePayload.STREAM_CODEC,
            SetPriorityConditionValuePayload::handle
        );
        registrar.playToClient(
            SyncPriorityConditionsPayload.TYPE,
            SyncPriorityConditionsPayload.STREAM_CODEC,
            SyncPriorityConditionsPayload::handle
        );
    }
}
