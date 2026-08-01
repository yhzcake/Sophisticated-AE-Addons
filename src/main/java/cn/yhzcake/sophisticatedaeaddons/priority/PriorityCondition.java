package cn.yhzcake.sophisticatedaeaddons.priority;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

@SuppressWarnings("null")
public record PriorityCondition(
    LogicOperator logicOp,
    boolean negated,
    AEKeyType keyType,
    ComparisonType comparisonType,
    ComparisonOperator comparisonOp,
    long value
) {
    public static final PriorityCondition DEFAULT = new PriorityCondition(
        LogicOperator.OR,
        false,
        AEKeyType.items(),
        ComparisonType.COUNT,
        ComparisonOperator.EQ,
        0
    );

    public static final Codec<PriorityCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            LogicOperator.CODEC.fieldOf("logic_op").forGetter(PriorityCondition::logicOp),
            Codec.BOOL.optionalFieldOf("negated", false).forGetter(PriorityCondition::negated),
            AEKeyType.CODEC.fieldOf("key_type").forGetter(PriorityCondition::keyType),
            ComparisonType.CODEC.fieldOf("comparison_type").forGetter(PriorityCondition::comparisonType),
            ComparisonOperator.CODEC.fieldOf("comparison_op").forGetter(PriorityCondition::comparisonOp),
            Codec.LONG.fieldOf("value").forGetter(PriorityCondition::value)
        ).apply(instance, PriorityCondition::decode)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PriorityCondition> STREAM_CODEC = StreamCodec.of(
        PriorityCondition::encode,
        PriorityCondition::decode
    );

    private static PriorityCondition decode(
        LogicOperator logicOp,
        boolean negated,
        AEKeyType keyType,
        ComparisonType comparisonType,
        ComparisonOperator comparisonOp,
        long value
    ) {
        ComparisonType normalizedType = keyType == AEKeyType.items() ? comparisonType : ComparisonType.COUNT;
        return new PriorityCondition(logicOp, negated, keyType, normalizedType, comparisonOp, value);
    }

    private static void encode(RegistryFriendlyByteBuf buffer, PriorityCondition condition) {
        LogicOperator.STREAM_CODEC.encode(buffer, condition.logicOp());
        buffer.writeBoolean(condition.negated());
        AEKeyType.STREAM_CODEC.encode(buffer, condition.keyType());
        ComparisonType.STREAM_CODEC.encode(buffer, condition.comparisonType());
        ComparisonOperator.STREAM_CODEC.encode(buffer, condition.comparisonOp());
        ByteBufCodecs.VAR_LONG.encode(buffer, condition.value());
    }

    private static PriorityCondition decode(RegistryFriendlyByteBuf buffer) {
        LogicOperator logicOp = LogicOperator.STREAM_CODEC.decode(buffer);
        boolean negated = buffer.readBoolean();
        AEKeyType keyType = AEKeyType.STREAM_CODEC.decode(buffer);
        ComparisonType comparisonType = ComparisonType.STREAM_CODEC.decode(buffer);
        ComparisonOperator comparisonOp = ComparisonOperator.STREAM_CODEC.decode(buffer);
        long value = ByteBufCodecs.VAR_LONG.decode(buffer);
        return decode(logicOp, negated, keyType, comparisonType, comparisonOp, value);
    }

    public boolean tests(AEKey incomingKey, long storedAmountAfterInsertion) {
        boolean result;
        if (incomingKey.getType() != keyType) {
            result = false;
        } else if (comparisonType == ComparisonType.STACK) {
            result = incomingKey instanceof AEItemKey itemKey && comparisonOp.test(itemKey.getMaxStackSize(), value);
        } else {
            result = comparisonOp.test(storedAmountAfterInsertion, value);
        }
        return negated ? !result : result;
    }

    public PriorityCondition withKeyType(AEKeyType type) {
        return new PriorityCondition(
            logicOp,
            negated,
            type,
            type == AEKeyType.items() ? comparisonType : ComparisonType.COUNT,
            comparisonOp,
            value
        );
    }

    public boolean apply(boolean accumulated, boolean current) {
        return switch (logicOp) {
            case AND -> accumulated && current;
            case OR -> accumulated || current;
            case XOR -> accumulated != current;
            case NAND -> !(accumulated && current);
            case NOR -> !(accumulated || current);
            case XNOR -> accumulated == current;
        };
    }

    public enum LogicOperator {
        AND, OR, XOR, NAND, NOR, XNOR;

        public static final Codec<LogicOperator> CODEC = Codec.STRING.xmap(LogicOperator::valueOf, LogicOperator::name);
        public static final StreamCodec<RegistryFriendlyByteBuf, LogicOperator> STREAM_CODEC =
            StreamCodec.of(
                (buf, val) -> buf.writeInt(val.ordinal()),
                buf -> values()[buf.readInt()]
            );

        public LogicOperator next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public LogicOperator previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }

    public enum ComparisonType {
        STACK, COUNT;

        public static final Codec<ComparisonType> CODEC = Codec.STRING.xmap(
            ComparisonType::valueOf, ComparisonType::name
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ComparisonType> STREAM_CODEC =
            StreamCodec.of(
                (buf, val) -> buf.writeInt(val.ordinal()),
                buf -> values()[buf.readInt()]
            );
    }

    public enum ComparisonOperator {
        GT(">"), LT("<"), EQ("="), GTE(">="), LTE("<="), NEQ("!=");

        private final String symbol;

        ComparisonOperator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public boolean test(long left, long right) {
            return switch (this) {
                case GT -> left > right;
                case LT -> left < right;
                case EQ -> left == right;
                case GTE -> left >= right;
                case LTE -> left <= right;
                case NEQ -> left != right;
            };
        }

        public static final Codec<ComparisonOperator> CODEC = Codec.STRING.xmap(
            ComparisonOperator::valueOf, ComparisonOperator::name
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ComparisonOperator> STREAM_CODEC =
            StreamCodec.of(
                (buf, val) -> buf.writeInt(val.ordinal()),
                buf -> values()[buf.readInt()]
            );

        public ComparisonOperator next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public ComparisonOperator previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }
}
