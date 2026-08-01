package cn.yhzcake.sophisticatedaeaddons;

import cn.yhzcake.sophisticatedaeaddons.blockentity.PlayerInterfaceBlockEntity;
import cn.yhzcake.sophisticatedaeaddons.blockentity.SophisticatedBackpackInterfaceBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SophisticatedAEAddons.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PlayerInterfaceBlockEntity>> PLAYER_INTERFACE =
        BLOCK_ENTITIES.register("player_interface",
            () -> BlockEntityType.Builder.of(PlayerInterfaceBlockEntity::new, ModBlocks.PLAYER_INTERFACE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SophisticatedBackpackInterfaceBlockEntity>> BACKPACK_INTERFACE =
        BLOCK_ENTITIES.register("backpack_interface",
            () -> BlockEntityType.Builder.of(SophisticatedBackpackInterfaceBlockEntity::new, ModBlocks.BACKPACK_INTERFACE.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
