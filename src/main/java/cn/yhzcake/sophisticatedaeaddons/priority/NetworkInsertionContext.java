package cn.yhzcake.sophisticatedaeaddons.priority;

import appeng.api.stacks.AEKey;

public final class NetworkInsertionContext {
    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    public static void begin(AEKey key, long stored, long requested) {
        STATE.set(new State(key, stored, requested));
    }

    public static void end() {
        STATE.remove();
    }

    public static long stored(AEKey key, long fallback) {
        State state = STATE.get();
        return state != null && state.key.equals(key) ? state.stored : fallback;
    }

    public static long requested(AEKey key, long fallback) {
        State state = STATE.get();
        return state != null && state.key.equals(key) ? state.requested : fallback;
    }

    public static void blockFallback(AEKey key) {
        State state = STATE.get();
        if (state != null && state.key.equals(key)) {
            state.fallbackBlocked = true;
        }
    }

    public static boolean isFallbackBlocked(AEKey key) {
        State state = STATE.get();
        return state != null && state.key.equals(key) && state.fallbackBlocked;
    }

    private static final class State {
        private final AEKey key;
        private final long stored;
        private final long requested;
        private boolean fallbackBlocked;

        private State(AEKey key, long stored, long requested) {
            this.key = key;
            this.stored = stored;
            this.requested = requested;
        }
    }

    private NetworkInsertionContext() {
    }
}
