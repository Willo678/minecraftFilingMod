package net.willo678.filingcabinet.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.willo678.filingcabinet.screen.FilingCabinetMenu;
import net.willo678.filingcabinet.util.Constants;

public class FilingCabinetBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {

    private NonNullList<ItemStack> items;

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int p_155466_, int p_155467_) {

        }

        @Override
        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof FilingCabinetMenu bfcMenu) {
                return true;
            } else {
                return false;
            }
        }
    };

    private final ChestLidController chestLidController = new ChestLidController();


    public FilingCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILING_CABINET.get(), pos, state);

        this.items = NonNullList.withSize(Constants.FILING_CABINET.TOTAL_SLOTS, ItemStack.EMPTY);
    }


    @Override
    public boolean triggerEvent(int id, int type) {
        if (id==1) {
            this.chestLidController.shouldBeOpen(type>0);
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inputItems) {
        this.items = NonNullList.withSize(Constants.FILING_CABINET.TOTAL_SLOTS, ItemStack.EMPTY);

        for (int i=0; i<inputItems.size(); i++) {
            if (i < this.items.size()) {
                this.getItems().set(i, inputItems.get(i));
            }
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(Constants.MODID+".container.filingcabinet_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FilingCabinetMenu(id, inv, this);
    }


    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public float getOpenNess(float partialTicks) {
        return this.chestLidController.getOpenness(partialTicks);
    }

    public static int getOpenCount(BlockGetter blockGetter, BlockPos blockPos) {
        BlockState blockState = blockGetter.getBlockState(blockPos);

        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);

            if (blockEntity instanceof FilingCabinetBlockEntity bfcBlockEntity) {
                return bfcBlockEntity.openersCounter.getOpenerCount();
            }
        }

        return 0;
    }


    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);

        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.items);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.items);
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FilingCabinetBlockEntity filingCabinetBlockEntity) {

    }
}
