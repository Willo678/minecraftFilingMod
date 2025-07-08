package net.willo678.filingcabinet.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.willo678.filingcabinet.block.entity.FilingCabinetBlockEntity;
import net.willo678.filingcabinet.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class FilingCabinetBlock extends BaseEntityBlock {


    public FilingCabinetBlock(Properties properties) {
        super(properties);
    }








    /* BLOCK ENTITY */

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState blockstate, Level level, BlockPos blockPos, BlockState newState, boolean isMoving) {
        if (blockstate.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof FilingCabinetBlockEntity bfcbEntity) {
                Containers.dropContents(level, blockPos, bfcbEntity);
                level.updateNeighborsAt(blockPos, this);
            }

        }

        super.onRemove(blockstate, level, blockPos, newState, isMoving);
    }


    @Override
    public InteractionResult use(BlockState pBlockState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof FilingCabinetBlockEntity) {
                NetworkHooks.openScreen(((ServerPlayer) pPlayer), (FilingCabinetBlockEntity)entity, pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(pLevel.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FilingCabinetBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pType) {
        return createTickerHelper(pType,
                ModBlockEntities.FILING_CABINET.get(),
                FilingCabinetBlockEntity::tick);
    }
}
