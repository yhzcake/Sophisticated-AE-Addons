package cn.yhzcake.sophisticatedaeaddons.priority;

public final class MigrationRoutingContext {
    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    public static boolean begin() {
        if (STATE.get() != null) {
            return false;
        }
        STATE.set(new State());
        return true;
    }

    public static void end() {
        STATE.remove();
    }

    public static boolean enterNetworkRouting() {
        State state = STATE.get();
        if (state == null || state.routing) {
            return false;
        }
        state.routing = true;
        return true;
    }

    public static void exitNetworkRouting() {
        State state = STATE.get();
        if (state != null) {
            state.routing = false;
        }
    }

    private static final class State {
        private boolean routing;
    }

    private MigrationRoutingContext() {
    }
}
