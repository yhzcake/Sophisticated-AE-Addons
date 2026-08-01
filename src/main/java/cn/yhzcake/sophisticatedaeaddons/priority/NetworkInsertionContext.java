package cn.yhzcake.sophisticatedaeaddons.priority;

import appeng.api.stacks.AEKey;

import java.util.ArrayDeque;
import java.util.Deque;

public final class NetworkInsertionContext {
    private static final ThreadLocal<Deque<State>> STATES = ThreadLocal.withInitial(ArrayDeque::new);

    public static void begin(AEKey key, long stored, long requested) {
        STATES.get().push(new State(key, stored, requested));
    }

    public static void end() {
        Deque<State> states = STATES.get();
        if (!states.isEmpty()) {
            states.pop();
        }
        if (states.isEmpty()) {
            STATES.remove();
        }
    }

    public static long stored(AEKey key, long fallback) {
        State state = current();
        return state != null && state.key.equals(key) ? state.stored : fallback;
    }

    public static long requested(AEKey key, long fallback) {
        State state = current();
        return state != null && state.key.equals(key) ? state.requested : fallback;
    }

    public static void blockFallback(AEKey key) {
        State state = current();
        if (state != null && state.key.equals(key)) {
            state.fallbackBlocked = true;
        }
    }

    public static boolean isFallbackBlocked(AEKey key) {
        State state = current();
        return state != null && state.key.equals(key) && state.fallbackBlocked;
    }

    private static State current() {
        Deque<State> states = STATES.get();
        return states.isEmpty() ? null : states.peek();
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
