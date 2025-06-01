package net.willo678.filingcabinet.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.willo678.filingcabinet.FilingMod;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;

public class FilingCabinetMenu extends AbstractContainerMenu {

    private final Container container;
    public static final ChestType chestType = Constants.FILING_CABINET;



    public FilingCabinetMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, playerInv.player.level.getBlockEntity(extraData.readBlockPos()), new SimpleContainer(chestType.TOTAL_SLOTS));
    }

    public FilingCabinetMenu(int id, Inventory playerInv, BlockEntity entity, Container inventory) {
        this(id, playerInv, inventory);
    }

    public FilingCabinetMenu(int id, Inventory playerInv, Container inventory) {
        super(ModMenuTypes.FILING_CABINET_MENU.get(), id);


        this.container = inventory;

        inventory.startOpen(playerInv.player);


        addInternalInventory(playerInv);
        addPlayerInventory(playerInv);
        addPlayerHotbar(playerInv);

    }

    private void addPlayerInventory(Inventory playerInv) {
        int leftCol = 12;//(this.chestType.xSize - 162) / 2 + 1;

        for (int playerInvRow = 0; playerInvRow < 3; playerInvRow++) {
            for (int playerInvCol = 0; playerInvCol < 9; playerInvCol++) {
                this.addSlot(new Slot(playerInv, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, chestType.ySize - (4 - playerInvRow) * 18 - 10));
            }

        }
    }

    private void addPlayerHotbar(Inventory playerInv) {
        int leftCol = 12;

        for (int hotHarSlot = 0; hotHarSlot < 9; hotHarSlot++) {
            this.addSlot(new Slot(playerInv, hotHarSlot, leftCol + hotHarSlot * 18, chestType.ySize - 24));
        }
    }


    private void addInternalInventory(Inventory playerInventory) {
        for (int chestRow = 0; chestRow < this.chestType.ROWS; chestRow++) {
            for (int chestCol = 0; chestCol < this.chestType.ROW_LENGTH; chestCol++) {
                this.addSlot(new Slot(playerInventory, chestCol + chestRow * this.chestType.ROW_LENGTH, 12 + chestCol * 18, 18 + chestRow * 18));
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

            if (index<this.chestType.TOTAL_SLOTS) {
                if (!this.moveItemStackTo(origin, this.chestType.TOTAL_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(origin, 0, this.chestType.TOTAL_SLOTS, false)) {
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
