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

    @Override
    public String toString() {
        return itemStack.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemWrapper wrapper) {
            return itemStack.sameItem(wrapper.itemStack);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack.getItem(), itemStack.getTag());
    }
}
