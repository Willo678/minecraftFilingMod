package net.willo678.filingcabinet.screen;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.willo678.filingcabinet.block.entity.FilingCabinetBlockEntity;
import net.willo678.filingcabinet.container.StorageSlot;
import net.willo678.filingcabinet.container.StoredItemStack;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class FilingCabinetMenu extends AbstractContainerMenu {

    public static final ChestType chestType = Constants.FILING_CABINET;

    private final FilingCabinetBlockEntity parent;
    private final Inventory playerInv;

    protected int playerSlotsStart;

    public List<StorageSlot> storageSlotList = new ArrayList<>();
    public List<StoredItemStack> itemList = new ArrayList<>();
    public List<ItemStack> sortedItemList = new ArrayList<>();

    public int scrollProgress;



    public FilingCabinetMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, new SimpleContainer(chestType.TOTAL_SLOTS), (FilingCabinetBlockEntity) playerInv.player.level.getBlockEntity(extraData.readBlockPos()));
    }

    public FilingCabinetMenu(int id, Inventory playerInv, Container inventory) {
        this(id, playerInv, inventory, null);
    }

    public FilingCabinetMenu(int id, Inventory playerInv, Container inventory, FilingCabinetBlockEntity entity) {
        super(ModMenuTypes.FILING_CABINET_MENU.get(), id);

        this.scrollProgress = 0;
        this.parent = entity;
        this.playerInv = playerInv;


        addInventorySlots(playerInv);
        inventory.startOpen(playerInv.player);
    }



    private void addInventorySlots(Inventory playerInv) {
        addInternalInventorySlots();

        this.playerSlotsStart = slots.size()-1;

        addPlayerInventorySlots(playerInv);
        addPlayerHotbarSlots(playerInv);
    }

    private void addInternalInventorySlots() {
        storageSlotList.clear();
        for (int chestRow = 0; chestRow < chestType.DISPLAY_ROWS; chestRow++) {
            for (int chestCol = 0; chestCol < chestType.ROW_LENGTH; chestCol++) {
                this.addSlotToContainer(new StorageSlot(parent, chestCol + chestRow * chestType.ROW_LENGTH, 12 + chestCol * 18, 18 + chestRow * 18));
            }
        }
    }
    private void addPlayerInventorySlots(Inventory playerInv) {
        int leftCol = 12;//(this.chestType.xSize - 162) / 2 + 1;

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new Slot(playerInv, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, chestType.ySize - (4 - playerInvRow) * 18 - 10));
            }

        }
    }
    private void addPlayerHotbarSlots(Inventory playerInv) {
        int leftCol = 12;

        for (int hotHarSlot = 0; hotHarSlot < 9; hotHarSlot++) {
            this.addSlot(new Slot(playerInv, hotHarSlot, leftCol + hotHarSlot * 18, chestType.ySize - 24));
        }
    }

    protected final void addSlotToContainer(StorageSlot storageSlot) {storageSlotList.add(storageSlot);}

    public final void getSlotContents(int id, StoredItemStack stack) {
        storageSlotList.get(id).stack = stack;
    }

    public final StorageSlot getSlotByID(int id) {
        return storageSlotList.get(id);
    }



    public enum SlotAction {
        PULL_OR_PUSH_STACK, PUSH_ONE, PULL_ONE, SPACE_CLICK, SHIFT_PULL, GET_HALF, GET_QUARTER
    }



    public void onInteract(StoredItemStack clicked, SlotAction action, boolean pullOne) {

        if (playerInv.player instanceof ServerPlayer serverPlayer) {serverPlayer.resetLastActionTime();}

        ItemStack carriedStack = getCarried();

        switch (action) {
            case PULL_OR_PUSH_STACK -> {
                if (!carriedStack.isEmpty()) {
                    StoredItemStack rem = parent.pushStack(new StoredItemStack(carriedStack));
                    ItemStack itemStack = (rem==null) ? ItemStack.EMPTY : rem.getActualStack();
                    setCarried(itemStack);
                } else {
                    if (clicked==null) {return;}
                    StoredItemStack pulled = parent.pullStack(clicked, clicked.getMaxStackSize());
                    if (pulled!=null) {
                        setCarried(pulled.getActualStack());
                    }
                }
            }
            case PUSH_ONE -> {
                if (!carriedStack.isEmpty()) {
                    parent.pushStack(new ItemStack(carriedStack.getItem(), 1));
                    if (carriedStack.getCount() > 1) {
                        carriedStack.setCount(carriedStack.getCount()-1);
                        setCarried(carriedStack);
                    }
                    else {setCarried(ItemStack.EMPTY);}
                }
            }
            case PULL_ONE -> {
                if (clicked==null) {return;}
                if (pullOne) {
                    StoredItemStack pulled = parent.pullStack(clicked, 1);
                    if (pulled!=null) {
                        ItemStack itemStack = pulled.getActualStack();
                        this.moveItemStackTo(itemStack, playerSlotsStart+1, this.slots.size(), true);
                        if (itemStack.getCount()>0) {parent.pushOrDrop(itemStack);}
                        playerInv.setChanged();
                    }
                } else {
                    if (!carriedStack.isEmpty()) {
                        if (ItemStack.isSameItemSameTags(carriedStack, clicked.getStack()) && carriedStack.getCount()+1 <= carriedStack.getMaxStackSize()) {
                            StoredItemStack pulled = parent.pullStack(clicked, 1);
                            if (pulled!=null) {
                                carriedStack.grow(1);
                            }
                        }
                    } else {
                        StoredItemStack pulled = parent.pullStack(clicked, 1);
                        if (pulled!=null) {
                            setCarried(pulled.getActualStack());
                        }
                    }
                }
            }
            case SPACE_CLICK -> {
                for (int i=playerSlotsStart+1; i<playerSlotsStart+28; i++) {
                    quickMoveStack(playerInv.player, i);
                }
            }
            case SHIFT_PULL -> {
                if (clicked==null) {return;}
                StoredItemStack pulled = parent.pullStack(clicked, clicked.getMaxStackSize());
                if (pulled!=null) {
                    ItemStack itemStack = pulled.getActualStack();
                    this.moveItemStackTo(itemStack, playerSlotsStart+1, this.slots.size(), true);
                    if (itemStack.getCount() > 0) {
                        parent.pushOrDrop(itemStack);
                    }
                    playerInv.setChanged();
                }
            }
            case GET_HALF -> {
                if (!carriedStack.isEmpty()) {
                    ItemStack stack1 = carriedStack.split(Math.max(Math.min(carriedStack.getCount(), carriedStack.getMaxStackSize()) / 2, 1));
                    ItemStack itemstack = parent.pushStack(stack1);
                    carriedStack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
                    setCarried(carriedStack);
                } else {
                    if (clicked == null) {
                        return;
                    }
                    StoredItemStack pulled = parent.pullStack(clicked, (int) Math.max(Math.min(clicked.getQuantity() / 2, clicked.getMaxStackSize() / 2), 1));
                    if (pulled != null) {
                        setCarried(pulled.getActualStack());
                    }
                }
            }
            case GET_QUARTER -> {
                if (!carriedStack.isEmpty()) {
                    ItemStack stack1 = carriedStack.split(Math.max(Math.min(carriedStack.getCount(), carriedStack.getMaxStackSize()) / 4, 1));
                    ItemStack itemstack = parent.pushStack(stack1);
                    carriedStack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
                    setCarried(carriedStack);
                } else {
                    if (clicked == null) return;
                    long maxCount = 64;
                    for (StoredItemStack e : itemList) {
                        if (e.equals(clicked)) maxCount = e.getQuantity();
                    }
                    StoredItemStack pulled = parent.pullStack(clicked, (int) Math.max(Math.min(maxCount, clicked.getMaxStackSize()) / 4, 1));
                    if (pulled != null) {
                        setCarried(pulled.getActualStack());
                    }
                }
            }
        }
    }





    public void incrementScrollProgress() {
        this.scrollProgress = Math.min(scrollProgress+1, (chestType.ROWS-chestType.DISPLAY_ROWS));

    }

    public void decrementScrollProgress() {
        this.scrollProgress = Math.max(scrollProgress-1, 0);

    }






    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot!=null && slot.hasItem()) {
            ItemStack origin = slot.getItem();
            itemStack = origin.copy();

            if (index < chestType.DISPLAY_TOTAL_SLOTS) {
                if (!this.moveItemStackTo(origin, chestType.DISPLAY_TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(origin, 0, chestType.DISPLAY_TOTAL_SLOTS, false)) {
              return ItemStack.EMPTY;
            }

            if (origin.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }


        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;//parent!=null;
    }






}
