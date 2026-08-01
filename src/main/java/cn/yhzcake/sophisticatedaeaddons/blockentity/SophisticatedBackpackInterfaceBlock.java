package cn.yhzcake.sophisticatedaeaddons.blockentity;

import cn.yhzcake.sophisticatedaeaddons.compat.BackpackCompat;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@SuppressWarnings("null")
public final class SophisticatedBackpackInterfaceBlock extends BaseEntityBlock {
    public static final MapCodec<SophisticatedBackpackInterfaceBlock> CODEC = simpleCodec(SophisticatedBackpackInterfaceBlock::new);

    public SophisticatedBackpackInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (BackpackCompat.isBackpack(stack)) {
            if (!level.isClientSide) {
                UUID uuid = BackpackCompat.getOrCreateBackpackUuid(stack);
                BlockEntity be = level.getBlockEntity(pos);
                if (uuid != null && be instanceof SophisticatedBackpackInterfaceBlockEntity sbie) {
                    sbie.bindBackpack(uuid);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SophisticatedBackpackInterfaceBlockEntity sbie && sbie.boundBackpackUuid() != null) {
                sbie.bindBackpack(null);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SophisticatedBackpackInterfaceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,
        BlockState state,
        BlockEntityType<T> blockEntityType
    ) {
        return level.isClientSide
            ? null
            : createTickerHelper(
                blockEntityType,
                cn.yhzcake.sophisticatedaeaddons.ModBlockEntities.BACKPACK_INTERFACE.get(),
                SophisticatedBackpackInterfaceBlockEntity::serverTick
            );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
