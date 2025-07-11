package net.willo678.filingcabinet.block;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.willo678.filingcabinet.block.entity.FilingCabinetBlockEntity;
import net.willo678.filingcabinet.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class FilingCabinetBlock extends HorizontalDirectionalBlock implements EntityBlock {

    private static final VoxelShape AABB_NORTH_HALF = Block.box(0, 0, 8, 16, 16, 16);
    private static final VoxelShape AABB_SOUTH_HALF = Block.box(0, 0, 0, 16, 16, 8);
    private static final VoxelShape AABB_WEST_HALF = Block.box(8, 0, 0, 16, 16, 16);
    private static final VoxelShape AABB_EAST_HALF = Block.box(0, 0, 0, 8, 16, 16);



    public FilingCabinetBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /* BLOCK ENTITY */

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> AABB_EAST_HALF;
            case WEST -> AABB_WEST_HALF;
            case SOUTH -> AABB_SOUTH_HALF;
            default -> AABB_NORTH_HALF;
        };
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
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

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int ID, int param) {
        super.triggerEvent(state, level, pos, ID, param);
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity!=null && blockentity.triggerEvent(ID, param);
    }

    @SuppressWarnings("deprecation")
    @ParametersAreNonnullByDefault
    @Override
    public @NotNull InteractionResult use(BlockState pBlockState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
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
    @ParametersAreNonnullByDefault
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FilingCabinetBlockEntity(pPos, pState);
    }


    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> entityTypeA, BlockEntityType<E> entityTypeB, BlockEntityTicker<? super E> bTicker) {
        return (entityTypeB == entityTypeA) ? (BlockEntityTicker<A>) bTicker : null;
    }

    @Nullable
    @ParametersAreNonnullByDefault
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pType) {
        return createTickerHelper(pType,
                ModBlockEntities.FILING_CABINET.get(),
                FilingCabinetBlockEntity::tick);
    }
}
