package cn.yhzcake.sophisticatedaeaddons.upgrade;

import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;

import java.util.function.Consumer;

@SuppressWarnings("null")
public final class AeBackpackUpgradeWrapper implements IUpgradeWrapper {
    private final IStorageWrapper storageWrapper;
    private final ItemStack upgradeStack;
    private final Consumer<ItemStack> saveHandler;

    public AeBackpackUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgradeStack,
                                     Consumer<ItemStack> saveHandler) {
        this.storageWrapper = storageWrapper;
        this.upgradeStack = upgradeStack;
        this.saveHandler = saveHandler;
    }

    @Override public boolean isEnabled() { return upgradeStack.getOrDefault(ModCoreDataComponents.ENABLED, true); }
    @Override public void setEnabled(boolean enabled) {
        upgradeStack.set(ModCoreDataComponents.ENABLED, enabled);
        saveHandler.accept(upgradeStack);
        storageWrapper.getUpgradeHandler().refreshWrappersThatImplementAndTypeWrappers();
    }
    @Override public boolean canBeDisabled() { return true; }
    @Override public ItemStack getUpgradeStack() { return upgradeStack; }
    @Override public void onAdded() {}
    @Override public void onBeforeRemoved() {}

    public IStorageWrapper getStorageWrapper() { return storageWrapper; }
}
