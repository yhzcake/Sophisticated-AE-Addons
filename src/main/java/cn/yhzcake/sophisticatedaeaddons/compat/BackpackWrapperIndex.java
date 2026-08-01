package cn.yhzcake.sophisticatedaeaddons.compat;

import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BackpackWrapperIndex {
    private static final ConcurrentHashMap<UUID, CopyOnWriteArrayList<WeakReference<IBackpackWrapper>>> WRAPPERS =
        new ConcurrentHashMap<>();

    public static void register(IBackpackWrapper wrapper) {
        wrapper.getContentsUuid().ifPresent(uuid -> {
            CopyOnWriteArrayList<WeakReference<IBackpackWrapper>> references =
                WRAPPERS.computeIfAbsent(uuid, ignored -> new CopyOnWriteArrayList<>());
            boolean registered = false;
            for (WeakReference<IBackpackWrapper> reference : references) {
                IBackpackWrapper existing = reference.get();
                if (existing == null) {
                    references.remove(reference);
                } else if (existing == wrapper) {
                    registered = true;
                }
            }
            if (!registered) {
                references.add(new WeakReference<>(wrapper));
            }
        });
    }

    @Nullable
    public static IBackpackWrapper find(UUID uuid) {
        List<WeakReference<IBackpackWrapper>> references = WRAPPERS.get(uuid);
        if (references == null) {
            return null;
        }
        for (WeakReference<IBackpackWrapper> reference : references) {
            IBackpackWrapper wrapper = reference.get();
            if (wrapper == null || !uuid.equals(wrapper.getContentsUuid().orElse(null))) {
                references.remove(reference);
                continue;
            }
            return wrapper;
        }
        WRAPPERS.remove(uuid);
        return null;
    }

    public static void synchronizePeers(IBackpackWrapper source) {
        source.getContentsUuid().ifPresent(uuid -> {
            List<WeakReference<IBackpackWrapper>> references = WRAPPERS.get(uuid);
            if (references == null) {
                return;
            }
            for (WeakReference<IBackpackWrapper> reference : references) {
                IBackpackWrapper wrapper = reference.get();
                if (wrapper == null || !uuid.equals(wrapper.getContentsUuid().orElse(null))) {
                    references.remove(reference);
                } else if (wrapper != source) {
                    wrapper.onContentsNbtUpdated();
                }
            }
        });
    }

    private BackpackWrapperIndex() {
    }
}
