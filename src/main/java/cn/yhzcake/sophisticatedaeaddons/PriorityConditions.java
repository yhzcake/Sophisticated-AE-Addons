package cn.yhzcake.sophisticatedaeaddons;

import appeng.api.stacks.AEKey;
import cn.yhzcake.sophisticatedaeaddons.priority.PriorityCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the list of priority conditions for the Precise Priority Card.
 * Stored as a data component on the card item.
 */
@SuppressWarnings("null")
public record PriorityConditions(List<PriorityCondition> conditions, MigrationMode migrationMode) {
    public static final PriorityConditions EMPTY = new PriorityConditions(List.of(PriorityCondition.DEFAULT), MigrationMode.OFF);

    public static final Codec<PriorityConditions> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            PriorityCondition.CODEC.listOf().optionalFieldOf("conditions", List.of(PriorityCondition.DEFAULT)).forGetter(PriorityConditions::conditions),
            MigrationMode.CODEC.optionalFieldOf("migration_mode", MigrationMode.OFF).forGetter(PriorityConditions::migrationMode)
        ).apply(instance, (conditions, migrationMode) -> new PriorityConditions(normalize(conditions), migrationMode))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PriorityConditions> STREAM_CODEC =
        StreamCodec.of(
            PriorityConditions::encode,
            PriorityConditions::decode
        );

    public PriorityConditions {
        conditions = normalize(conditions);
    }

    private static List<PriorityCondition> normalize(List<PriorityCondition> conditions) {
        return conditions == null || conditions.isEmpty() ? List.of(PriorityCondition.DEFAULT) : List.copyOf(conditions);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, PriorityConditions conditions) {
        buffer.writeVarInt(conditions.conditions().size());
        for (PriorityCondition condition : conditions.conditions()) {
            PriorityCondition.STREAM_CODEC.encode(buffer, condition);
        }
        buffer.writeVarInt(conditions.migrationMode().ordinal());
    }

    private static PriorityConditions decode(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<PriorityCondition> conditions = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            conditions.add(PriorityCondition.STREAM_CODEC.decode(buffer));
        }
        MigrationMode migrationMode = MigrationMode.values()[buffer.readVarInt()];
        return new PriorityConditions(conditions, migrationMode);
    }

    public PriorityConditions withCondition(PriorityCondition condition) {
        List<PriorityCondition> newList = new ArrayList<>(conditions);
        newList.add(condition);
        return new PriorityConditions(newList, migrationMode);
    }

    public PriorityConditions withoutCondition(int index) {
        if (index < 0 || index >= conditions.size()) {
            return this;
        }
        if (conditions.size() == 1) {
            return new PriorityConditions(List.of(PriorityCondition.DEFAULT), migrationMode);
        }
        List<PriorityCondition> newList = new ArrayList<>(conditions);
        newList.remove(index);
        return new PriorityConditions(newList, migrationMode);
    }

    public PriorityConditions withMigrationMode(MigrationMode mode) {
        return new PriorityConditions(conditions, mode);
    }

    public boolean test(AEKey key, long storedAmountAfterInsertion) {
        boolean result = true;
        for (PriorityCondition condition : conditions) {
            result = condition.apply(result, condition.tests(key, storedAmountAfterInsertion));
        }
        return result;
    }

    public enum MigrationMode {
        OFF,
        ON,
        FORCE;

        public static final Codec<MigrationMode> CODEC = Codec.STRING.xmap(MigrationMode::valueOf, MigrationMode::name);

        public MigrationMode next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }
}
