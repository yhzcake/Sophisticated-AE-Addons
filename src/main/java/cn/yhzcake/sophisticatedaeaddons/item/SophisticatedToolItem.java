package cn.yhzcake.sophisticatedaeaddons.item;

import cn.yhzcake.sophisticatedaeaddons.ModDataComponents;
import cn.yhzcake.sophisticatedaeaddons.ModItems;
import cn.yhzcake.sophisticatedaeaddons.UpgradeToolContents;
import cn.yhzcake.sophisticatedaeaddons.UpgradeToolMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SophisticatedToolItem extends Item {
    private static final Component TITLE = Component.translatable("container.sophisticated_ae_addons.upgrade_tool");

    public SophisticatedToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            int toolSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new UpgradeToolMenu(id, inv, toolSlot),
                TITLE
            ), buffer -> buffer.writeVarInt(toolSlot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static UpgradeToolContents getContents(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.UPGRADE_TOOL_CONTENTS, UpgradeToolContents.EMPTY);
    }

    public static void setContents(ItemStack stack, UpgradeToolContents contents) {
        stack.set(ModDataComponents.UPGRADE_TOOL_CONTENTS, contents);
    }
}
