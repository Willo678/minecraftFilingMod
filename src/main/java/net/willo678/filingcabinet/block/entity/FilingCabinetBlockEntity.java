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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.willo678.filingcabinet.container.StoredItemStack;
import net.willo678.filingcabinet.screen.FilingCabinetMenu;
import net.willo678.filingcabinet.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class FilingCabinetBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {

    public HashMap<Item, Integer> items; //Consider appropriate storage data structure

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

        this.items = new HashMap<>();
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
    public NonNullList<ItemStack> getItems() {
        NonNullList<ItemStack> itemList = NonNullList.createWithCapacity(this.items.size());

        itemList.clear();
        for (Map.Entry<Item, Integer> entry : items.entrySet()) {
            itemList.add(new ItemStack(entry.getKey(), entry.getValue()));
        }

        return itemList;
    }

    @Override
    public void setItems(NonNullList<ItemStack> inputItems) {
        this.items = new HashMap<>();

        for (ItemStack itemStack : inputItems) {
            if (itemStack == ItemStack.EMPTY) {continue;}

            this.items.merge(itemStack.getItem(), itemStack.getCount(), Integer::sum);
        }

    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(Constants.MODID+".container.filingcabinet_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
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





    public StoredItemStack pullStack(StoredItemStack stack, int max) {
        if (stack==null || max<=0) {return null;}

        ItemStack st = stack.getStack();
        Item item = st.getItem();

        if (items.containsKey(item)) {
            int count = Math.min(max, items.get(item));

            items.merge(st.getItem(), -count, Integer::sum);
            if (items.get(item)<=0) {items.remove(item);}

            debugItems();
            setChanged();
            return new StoredItemStack(new ItemStack(st.getItem(), count));
        } else {
            debugItems();
            return null;
        }
    }

    public StoredItemStack pushStack(StoredItemStack stack) {
        if (stack==null) {return null;}
        ItemStack copyStack = stack.getActualStack().copy();
        int amount = copyStack.getCount();
        int remainder = copyStack.getCount() - amount;

        items.merge(copyStack.getItem(), amount, Integer::sum);

        debugItems();
        setChanged();
        return (remainder>0) ? new StoredItemStack(new ItemStack(copyStack.getItem(), remainder)) : null;
    }

    public ItemStack pushStack(ItemStack itemStack) {
        StoredItemStack is = pushStack(new StoredItemStack(itemStack));
        debugItems();
        setChanged();
        return (is==null) ? ItemStack.EMPTY : is.getActualStack();
    }

    public void pushOrDrop(ItemStack itemStack) {
        if (itemStack.isEmpty()) {return;}
        StoredItemStack st0 = pushStack(new StoredItemStack(itemStack));
        if (st0!=null) {
            Containers.dropItemStack(level, worldPosition.getX()+0.5f, worldPosition.getY()+0.5f, worldPosition.getZ()+0.5f, st0.getActualStack());
        }
        debugItems();
    }









    @Override
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
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);

        if (!this.trySaveLootTable(compoundTag)) {
            NonNullList<ItemStack> saveItems = getItems();
            compoundTag.putInt("l", saveItems.size());
            ContainerHelper.saveAllItems(compoundTag, saveItems);
        }
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FilingCabinetBlockEntity filingCabinetBlockEntity) {
    }


    public void debugItems() {
        Constants.log("Items:");
        for (Map.Entry<Item, Integer> e : items.entrySet()) {
            Constants.log("  Item: "+e.getValue()+" "+e.getKey());
        }
    }
}
