package cn.yhzcake.sophisticatedaeaddons.mixin;

import appeng.api.stacks.AEKeyType;
import appeng.helpers.IPriorityHost;
import appeng.menu.implementations.PriorityMenu;
import appeng.parts.storagebus.StorageBusPart;
import cn.yhzcake.sophisticatedaeaddons.ModDataComponents;
import cn.yhzcake.sophisticatedaeaddons.ModItems;
import cn.yhzcake.sophisticatedaeaddons.PriorityConditions;
import cn.yhzcake.sophisticatedaeaddons.network.SyncPriorityConditionsPayload;
import cn.yhzcake.sophisticatedaeaddons.priority.PrecisePriorityMenuExtension;
import cn.yhzcake.sophisticatedaeaddons.priority.PriorityCondition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("null")
@Mixin(value = PriorityMenu.class, remap = false)
public abstract class MixinPriorityMenu implements PrecisePriorityMenuExtension {
    @Unique
    private int sophisticatedAeAddons$scrollOffset;
    @Unique
    private int sophisticatedAeAddons$selectedIndex = -1;
    @Unique
    private boolean sophisticatedAeAddons$loading;
    @Unique
    private ServerPlayer sophisticatedAeAddons$serverPlayer;
    @Unique
    private boolean sophisticatedAeAddons$syncedHasCard;
    @Unique
    private PriorityConditions sophisticatedAeAddons$syncedConditions = PriorityConditions.EMPTY;
    @Unique
    private boolean sophisticatedAeAddons$lastSentHasCard;
    @Unique
    private PriorityConditions sophisticatedAeAddons$lastSentConditions;
    @Unique
    private int sophisticatedAeAddons$lastSentScrollOffset = -1;
    @Unique
    private int sophisticatedAeAddons$lastSentSelectedIndex = -2;

    @Inject(
        method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPriorityHost;)V",
        at = @At("TAIL"),
        require = 1
    )
    private void sophisticatedAeAddons$initialize(
        int id,
        Inventory inventory,
        IPriorityHost host,
        CallbackInfo ci
    ) {
        if (inventory.player instanceof ServerPlayer serverPlayer) {
            sophisticatedAeAddons$serverPlayer = serverPlayer;
        }
    }

    @Override
    public boolean sophisticatedAeAddons$hasCard() {
        if (sophisticatedAeAddons$serverPlayer == null) {
            return sophisticatedAeAddons$syncedHasCard;
        }
        return sophisticatedAeAddons$cardSlot() >= 0;
    }

    @Override
    public PriorityConditions sophisticatedAeAddons$getConditions() {
        if (sophisticatedAeAddons$serverPlayer == null) {
            return sophisticatedAeAddons$syncedConditions;
        }
        StorageBusPart host = sophisticatedAeAddons$storageBusHost();
        int slot = sophisticatedAeAddons$cardSlot();
        if (host == null || slot < 0) {
            return PriorityConditions.EMPTY;
        }
        return host.getUpgrades().getStackInSlot(slot).getOrDefault(
            ModDataComponents.PRIORITY_CONDITIONS,
            PriorityConditions.EMPTY
        );
    }

    @Override
    public int sophisticatedAeAddons$getScrollOffset() {
        return sophisticatedAeAddons$scrollOffset;
    }

    @Override
    public int sophisticatedAeAddons$getSelectedIndex() {
        return sophisticatedAeAddons$selectedIndex;
    }

    @Override
    public void sophisticatedAeAddons$handleAction(Action action, int index, long value) {
        if (sophisticatedAeAddons$serverPlayer == null || !sophisticatedAeAddons$hasCard()) {
            return;
        }
        PriorityConditions conditions = sophisticatedAeAddons$getConditions();
        if (action == Action.SET_SCROLL_OFFSET) {
            sophisticatedAeAddons$scrollOffset = sophisticatedAeAddons$clampOffset((int) value, conditions.conditions().size());
            sophisticatedAeAddons$sync();
            return;
        }
        if (action == Action.CLEAR_SELECTION) {
            sophisticatedAeAddons$selectedIndex = -1;
            sophisticatedAeAddons$sync();
            return;
        }
        if (action == Action.ADD) {
            int requestedIndex = index >= 0 && index < conditions.conditions().size() ? index : sophisticatedAeAddons$selectedIndex;
            int insertionIndex = requestedIndex >= 0
                && requestedIndex < conditions.conditions().size()
                ? requestedIndex
                : conditions.conditions().size();
            List<PriorityCondition> list = new ArrayList<>(conditions.conditions());
            list.add(insertionIndex, PriorityCondition.DEFAULT);
            sophisticatedAeAddons$selectedIndex = insertionIndex;
            sophisticatedAeAddons$ensureVisible(insertionIndex);
            sophisticatedAeAddons$writeWithoutSync(new PriorityConditions(list, conditions.migrationMode()));
            sophisticatedAeAddons$sync();
            return;
        }
        if (action == Action.CYCLE_MIGRATION) {
            sophisticatedAeAddons$write(conditions.withMigrationMode(conditions.migrationMode().next()));
            return;
        }
        if (index < 0 || index >= conditions.conditions().size()) {
            return;
        }
        if (action == Action.SELECT) {
            sophisticatedAeAddons$selectedIndex = index;
            sophisticatedAeAddons$sync();
            return;
        }
        sophisticatedAeAddons$selectedIndex = index;
        if (action == Action.DELETE) {
            PriorityConditions replacement = conditions.withoutCondition(index);
            sophisticatedAeAddons$selectedIndex = replacement.conditions().size() == 1 && conditions.conditions().size() == 1
                ? 0
                : Math.min(index, replacement.conditions().size() - 1);
            sophisticatedAeAddons$write(replacement);
            sophisticatedAeAddons$scrollOffset = sophisticatedAeAddons$clampOffset(
                sophisticatedAeAddons$scrollOffset,
                replacement.conditions().size()
            );
            sophisticatedAeAddons$sync();
            return;
        }
        PriorityCondition old = conditions.conditions().get(index);
        PriorityCondition replacement = switch (action) {
            case CYCLE_LOGIC -> new PriorityCondition(
                value < 0 ? old.logicOp().previous() : old.logicOp().next(),
                old.negated(), old.keyType(), old.comparisonType(), old.comparisonOp(), old.value()
            );
            case TOGGLE_NEGATED -> new PriorityCondition(
                old.logicOp(), !old.negated(), old.keyType(), old.comparisonType(), old.comparisonOp(), old.value()
            );
            case CYCLE_KEY_TYPE -> old.withKeyType(sophisticatedAeAddons$cycleKeyType(old.keyType(), value < 0));
            case CYCLE_TYPE -> new PriorityCondition(
                old.logicOp(), old.negated(), old.keyType(),
                old.keyType() == AEKeyType.items() && old.comparisonType() == PriorityCondition.ComparisonType.COUNT
                    ? PriorityCondition.ComparisonType.STACK
                    : PriorityCondition.ComparisonType.COUNT,
                old.comparisonOp(), old.value()
            );
            case CYCLE_COMPARISON -> new PriorityCondition(
                old.logicOp(), old.negated(), old.keyType(), old.comparisonType(),
                value < 0 ? old.comparisonOp().previous() : old.comparisonOp().next(), old.value()
            );
            case SET_VALUE -> new PriorityCondition(
                old.logicOp(), old.negated(), old.keyType(), old.comparisonType(), old.comparisonOp(), value
            );
            default -> null;
        };
        if (replacement != null) {
            sophisticatedAeAddons$replace(index, replacement);
        }
    }

    @Override
    public void sophisticatedAeAddons$applySync(
        boolean hasCard,
        PriorityConditions conditions,
        int scrollOffset,
        int selectedIndex
    ) {
        sophisticatedAeAddons$syncedHasCard = hasCard;
        sophisticatedAeAddons$syncedConditions = conditions;
        sophisticatedAeAddons$scrollOffset = sophisticatedAeAddons$clampOffset(scrollOffset, conditions.conditions().size());
        sophisticatedAeAddons$selectedIndex = selectedIndex >= 0 && selectedIndex < conditions.conditions().size()
            ? selectedIndex
            : -1;
    }

    @Override
    public void sophisticatedAeAddons$syncIfChanged() {
        sophisticatedAeAddons$sync();
    }

    @Unique
    private StorageBusPart sophisticatedAeAddons$storageBusHost() {
        IPriorityHost host = ((PriorityMenu) (Object) this).getHost();
        return host instanceof StorageBusPart storageBus ? storageBus : null;
    }

    @Unique
    private int sophisticatedAeAddons$cardSlot() {
        StorageBusPart host = sophisticatedAeAddons$storageBusHost();
        if (host == null) {
            return -1;
        }
        for (int slot = 0; slot < host.getUpgrades().size(); slot++) {
            if (host.getUpgrades().getStackInSlot(slot).is(ModItems.PRECISE_PRIORITY_CARD.get())) {
                return slot;
            }
        }
        return -1;
    }

    @Unique
    private void sophisticatedAeAddons$replace(int index, PriorityCondition replacement) {
        PriorityConditions conditions = sophisticatedAeAddons$getConditions();
        List<PriorityCondition> list = new ArrayList<>(conditions.conditions());
        list.set(index, replacement);
        sophisticatedAeAddons$write(new PriorityConditions(list, conditions.migrationMode()));
    }

    @Unique
    private void sophisticatedAeAddons$write(PriorityConditions conditions) {
        StorageBusPart host = sophisticatedAeAddons$storageBusHost();
        int slot = sophisticatedAeAddons$cardSlot();
        if (host == null || slot < 0) {
            return;
        }
        ItemStack card = host.getUpgrades().getStackInSlot(slot);
        card.set(ModDataComponents.PRIORITY_CONDITIONS, conditions);
        host.getUpgrades().setItemDirect(slot, card);
        host.upgradesChanged();
        sophisticatedAeAddons$sync();
    }

    @Unique
    private void sophisticatedAeAddons$writeWithoutSync(PriorityConditions conditions) {
        StorageBusPart host = sophisticatedAeAddons$storageBusHost();
        int slot = sophisticatedAeAddons$cardSlot();
        if (host == null || slot < 0) {
            return;
        }
        ItemStack card = host.getUpgrades().getStackInSlot(slot);
        card.set(ModDataComponents.PRIORITY_CONDITIONS, conditions);
        host.getUpgrades().setItemDirect(slot, card);
        host.upgradesChanged();
    }

    @Unique
    private int sophisticatedAeAddons$clampOffset(int offset, int size) {
        return Math.max(0, Math.min(offset, Math.max(0, size - VISIBLE_ROWS)));
    }

    @Unique
    private void sophisticatedAeAddons$ensureVisible(int index) {
        if (index < sophisticatedAeAddons$scrollOffset) {
            sophisticatedAeAddons$scrollOffset = index;
        } else if (index >= sophisticatedAeAddons$scrollOffset + VISIBLE_ROWS) {
            sophisticatedAeAddons$scrollOffset = index - VISIBLE_ROWS + 1;
        }
        sophisticatedAeAddons$scrollOffset = sophisticatedAeAddons$clampOffset(
            sophisticatedAeAddons$scrollOffset,
            sophisticatedAeAddons$getConditions().conditions().size()
        );
    }

    @Unique
    private AEKeyType sophisticatedAeAddons$cycleKeyType(AEKeyType current, boolean backwards) {
        List<AEKeyType> types = sophisticatedAeAddons$serverPlayer.level().registryAccess()
            .registryOrThrow(AEKeyType.REGISTRY_KEY)
            .stream()
            .sorted(java.util.Comparator.comparing(AEKeyType::getId))
            .toList();
        int index = types.indexOf(current);
        int direction = backwards ? types.size() - 1 : 1;
        return types.get((index + direction) % types.size());
    }

    @Unique
    private void sophisticatedAeAddons$sync() {
        if (sophisticatedAeAddons$serverPlayer == null) {
            return;
        }
        boolean hasCard = sophisticatedAeAddons$hasCard();
        PriorityConditions conditions = sophisticatedAeAddons$getConditions();
        if (sophisticatedAeAddons$lastSentConditions != null
            && sophisticatedAeAddons$lastSentHasCard == hasCard
            && sophisticatedAeAddons$lastSentConditions.equals(conditions)
            && sophisticatedAeAddons$lastSentScrollOffset == sophisticatedAeAddons$scrollOffset
            && sophisticatedAeAddons$lastSentSelectedIndex == sophisticatedAeAddons$selectedIndex) {
            return;
        }
        sophisticatedAeAddons$lastSentHasCard = hasCard;
        sophisticatedAeAddons$lastSentConditions = conditions;
        sophisticatedAeAddons$lastSentScrollOffset = sophisticatedAeAddons$scrollOffset;
        sophisticatedAeAddons$lastSentSelectedIndex = sophisticatedAeAddons$selectedIndex;
        PriorityMenu self = (PriorityMenu) (Object) this;
        PacketDistributor.sendToPlayer(
            sophisticatedAeAddons$serverPlayer,
            new SyncPriorityConditionsPayload(
                self.containerId,
                hasCard,
                conditions,
                sophisticatedAeAddons$scrollOffset,
                sophisticatedAeAddons$selectedIndex
            )
        );
    }
}
