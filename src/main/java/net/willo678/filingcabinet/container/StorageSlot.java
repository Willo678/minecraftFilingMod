package net.willo678.filingcabinet.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class StorageSlot {

    public int xDisplayPosition;
    public int yDisplayPosition;

    private final int slotIndex;

    public final Container inventory;
    public ItemStack stack;

    public StorageSlot(Container inventory, int slotIndex, int xPosition, int yPosition) {
        this.xDisplayPosition = xPosition;
        this.yDisplayPosition = yPosition;

        this.slotIndex = slotIndex;
        this.inventory = inventory;
    }

}
