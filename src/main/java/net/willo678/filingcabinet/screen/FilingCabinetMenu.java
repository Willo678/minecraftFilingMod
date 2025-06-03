package net.willo678.filingcabinet.screen;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.willo678.filingcabinet.block.entity.FilingCabinetBlockEntity;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;

public class FilingCabinetMenu extends AbstractContainerMenu {

    public static final ChestType chestType = Constants.FILING_CABINET;


    private NonNullList<ItemStack> items;
    private final Container container;

    public int scrollProgress;



    public FilingCabinetMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainer(chestType.DISPLAY_TOTAL_SLOTS));
    }

    public FilingCabinetMenu(int id, Inventory playerInv, BlockEntity entity, Container inventory) {
        this(id, playerInv, inventory);
    }

    public FilingCabinetMenu(int id, Inventory playerInv, Container inventory) {
        super(ModMenuTypes.FILING_CABINET_MENU.get(), id);

        this.scrollProgress = 0;
        this.container = inventory;

        this.items = NonNullList.withSize(Constants.FILING_CABINET.TOTAL_SLOTS, ItemStack.EMPTY);
        for (int i=0; i<chestType.DISPLAY_TOTAL_SLOTS-1; i++) {this.items.set(i, container.getItem(i));}


        inventory.startOpen(playerInv.player);

        addInventorySlots(playerInv, inventory);

        //this.scrollTo(0);
    }

    private void addInventorySlots(Inventory playerInv, Container inventory) {
        addInternalInventorySlots(inventory);
        addPlayerInventorySlots(playerInv);
        addPlayerHotbarSlots(playerInv);
    }

    private void addInternalInventorySlots(Container inventory) {
        for (int chestRow = 0; chestRow < chestType.DISPLAY_ROWS; chestRow++) {
            for (int chestCol = 0; chestCol < chestType.ROW_LENGTH; chestCol++) {
                this.addSlot(new Slot(inventory, chestCol + chestRow * chestType.ROW_LENGTH, 12 + chestCol * 18, 18 + chestRow * 18));
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



    public void scrollTo(float scroll) {

        int i = (this.items.size() / 9) - 4;
        int startRow = (int)((double)(scroll * (float) i) + 0.5D);

        if (startRow < 0) {startRow=0;}

        for (int row=0; row<(chestType.DISPLAY_ROWS-1); row++) {
            for (int col=0; col<(chestType.ROW_LENGTH-1); col++) {
                int index = col + (row + startRow) * 9;

                this.container.setItem(col + row*9, ((index>=0 && index<items.size()) ? this.items.get(index) : ItemStack.EMPTY));
            }

        }

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
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player playerInv) {
        super.removed(playerInv);
        this.container.stopOpen(playerInv);
    }
}
