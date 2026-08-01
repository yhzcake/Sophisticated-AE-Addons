package cn.yhzcake.sophisticatedaeaddons;

import cn.yhzcake.sophisticatedaeaddons.blockentity.PlayerInterfaceBlock;
import cn.yhzcake.sophisticatedaeaddons.blockentity.SophisticatedBackpackInterfaceBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
@SuppressWarnings("null")
public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, SophisticatedAEAddons.MOD_ID);

    public static final DeferredHolder<Block, PlayerInterfaceBlock> PLAYER_INTERFACE =
        BLOCKS.register("player_interface", () -> new PlayerInterfaceBlock(BlockBehaviour.Properties.of().strength(2.5F).noOcclusion()));

    public static final DeferredHolder<Block, SophisticatedBackpackInterfaceBlock> BACKPACK_INTERFACE =
        BLOCKS.register("sophisticated_backpack_interface", () -> new SophisticatedBackpackInterfaceBlock(BlockBehaviour.Properties.of().strength(2.5F).noOcclusion()));

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
