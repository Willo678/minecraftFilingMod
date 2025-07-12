package net.willo678.filingcabinet.util;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ItemWrapper {
    protected ItemStack itemStack;

    public ItemWrapper(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }


    public static ItemWrapper getSingleItemWrapper(ItemStack itemStack) {
        return new ItemWrapper(getSingleItemstack(itemStack));
    }

    public static ItemStack getSingleItemstack(ItemStack itemStack) {
        ItemStack toReturn = itemStack.copy();
        toReturn.setCount(1);
        return toReturn;
    }

    @Override
    public String toString() {
        return itemStack.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemWrapper wrapper) {
            return ItemStack.isSame(this.itemStack, wrapper.itemStack)
                    && ItemStack.isSameItemSameTags(this.itemStack, wrapper.itemStack);
                    //&& this.itemStack.getCount() == wrapper.itemStack.getCount();
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack.getItem(), itemStack.getTag());//, itemStack.getCount());
    }
}
