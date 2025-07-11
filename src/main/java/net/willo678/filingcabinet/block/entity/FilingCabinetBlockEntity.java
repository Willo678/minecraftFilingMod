package net.willo678.filingcabinet.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.willo678.filingcabinet.container.StoredItemStack;
import net.willo678.filingcabinet.screen.FilingCabinetMenu;
import net.willo678.filingcabinet.util.Constants;
import net.willo678.filingcabinet.util.SingleItemHolder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class FilingCabinetBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {

    public SingleItemHolder items; //Consider appropriate storage data structure

    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {

        @Override
        @ParametersAreNonnullByDefault
        protected void onOpen(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        @ParametersAreNonnullByDefault
        protected void onClose(Level level, BlockPos blockPos, BlockState blockState) {

        }

        @Override
        @ParametersAreNonnullByDefault
        protected void openerCountChanged(Level level, BlockPos blockPos, BlockState blockState, int i, int i1) {

        }

        @Override
        protected boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof FilingCabinetMenu bfcMenu;
        }
    };

    private final ChestLidController chestLidController = new ChestLidController();
    private String lastSearch = "";


    public FilingCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILING_CABINET.get(), pos, state);

        this.items = new SingleItemHolder();
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
    public void startOpen(@NotNull Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(@NotNull Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public @NotNull NonNullList<ItemStack> getItems() {
        return items.toNonNullList();
    }

    @Override
    public void setItems(@NotNull NonNullList<ItemStack> inputItems) {
        items = SingleItemHolder.fromNonNullList(inputItems);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable(Constants.MODID+".container.filingcabinet_chest");
    }

    @Override
    @ParametersAreNonnullByDefault
    protected @NotNull AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new FilingCabinetMenu(id, inv, this, this);
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





    public @Nullable StoredItemStack pullStack(StoredItemStack stack, int max) {
        if (stack==null || max<=0) {return null;}

        ItemStack st = items.popItem(stack.getStack(), max);

        if (st!=null) {setChanged();}

        return new StoredItemStack(st);

    }

    @SuppressWarnings("ConstantValue")
    public StoredItemStack pushStack(StoredItemStack stack) {
        if (stack==null) {return null;}
        ItemStack copyStack = stack.getActualStack().copy();
        //Change 'amount' to any other value if you want to limit maximum insertion at a time
        int amount = copyStack.getCount();
        int remainder = copyStack.getCount() - amount;

        boolean success = items.putItem(copyStack, amount);

        if (success) {
            setChanged();
            return (remainder > 0) ? new StoredItemStack(new ItemStack(copyStack.getItem(), remainder)) : null;
        } else {
            return stack;
        }
    }

    public ItemStack pushStack(ItemStack itemStack) {
        StoredItemStack is = pushStack(new StoredItemStack(itemStack));
        setChanged();
        return (is==null) ? ItemStack.EMPTY : is.getActualStack();
    }

    public void pushOrDrop(ItemStack itemStack) {
        if (itemStack.isEmpty()) {return;}
        StoredItemStack st0 = pushStack(new StoredItemStack(itemStack));
        if (st0!=null) {
            Containers.dropItemStack(level, worldPosition.getX()+0.5f, worldPosition.getY()+0.5f, worldPosition.getZ()+0.5f, st0.getActualStack());
        }
    }









    @Override
    @ParametersAreNonnullByDefault
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);

        if (!this.tryLoadLootTable(compoundTag)) {
            int size = (compoundTag.contains("l")) ? compoundTag.getInt("l") : 0;
            NonNullList<ItemStack> loadedItems = NonNullList.withSize(size, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(compoundTag, loadedItems);
            setItems(loadedItems);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (!this.trySaveLootTable(compoundTag)) {
            NonNullList<ItemStack> saveItems = getItems();
            compoundTag.putInt("l", saveItems.size());
            ContainerHelper.saveAllItems(compoundTag, saveItems);
        }
    }

    @SuppressWarnings("unused")
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FilingCabinetBlockEntity filingCabinetBlockEntity) {

    }



    public String getLastSearch() {
        return lastSearch;
    }

    public void setLastSearch(String string) {
        lastSearch = string;
    }
}
