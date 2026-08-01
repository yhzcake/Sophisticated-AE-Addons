package cn.yhzcake.sophisticatedaeaddons.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeSlotChangeResult;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("null")
public final class AeBackpackUpgradeItem extends Item implements IUpgradeItem<AeBackpackUpgradeWrapper> {
    private static final UpgradeType<AeBackpackUpgradeWrapper> TYPE = new UpgradeType<>(AeBackpackUpgradeWrapper::new);

    public AeBackpackUpgradeItem(Properties properties) {
        super(properties);
    }

    @Override
    public UpgradeType<AeBackpackUpgradeWrapper> getType() {
        return TYPE;
    }

    @Override
    public UpgradeSlotChangeResult canAddUpgradeTo(
        IStorageWrapper storageWrapper,
        ItemStack upgradeStack,
        boolean firstLevelStorage,
        boolean clientSide
    ) {
        return IUpgradeItem.super.canAddUpgradeTo(
            storageWrapper,
            upgradeStack,
            firstLevelStorage,
            clientSide
        );
    }

    @Override
    public List<UpgradeConflictDefinition> getUpgradeConflicts() {
        return Collections.emptyList();
    }

    @Override
    public int getUpgradesPerStorage(String storageType) {
        return 1;
    }

    @Override
    public int getUpgradesInGroupPerStorage(String storageType) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Component getName() {
        return getDescription();
    }
}
