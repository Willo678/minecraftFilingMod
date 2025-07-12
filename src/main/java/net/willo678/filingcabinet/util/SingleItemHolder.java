package net.willo678.filingcabinet.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import static net.willo678.filingcabinet.util.ItemWrapper.getSingleItemWrapper;

import java.util.*;
import java.util.function.BiConsumer;

@SuppressWarnings("UnusedReturnValue")
public class SingleItemHolder {
    private Item itemType;
    private Map<ItemWrapper, Integer> items;

    public SingleItemHolder() {
        itemType = null;
        items = new HashMap<>();
    }

    public boolean canContainItem(ItemWrapper itemWrapper) {return canContainItem(itemWrapper.getItemStack());}
    public boolean canContainItem(ItemStack itemStack) {return (!itemStack.isEmpty()) && ((itemType==null) || (itemStack.getItem()==itemType));}

    public boolean putItem(ItemStack itemStack, int quantity) {
        if (!canContainItem(itemStack)) {return false;}
        itemType = itemStack.getItem();
        items.merge(getSingleItemWrapper(itemStack), quantity, Integer::sum);
        return true;
    }

    public boolean putItem(@NotNull ItemStack itemStack) {
        if (!canContainItem(itemStack)) {return false;}
        itemType = itemStack.getItem();
        items.merge(getSingleItemWrapper(itemStack), itemStack.getCount(), Integer::sum);
        return true;
    }

    public boolean setItem(@NotNull ItemStack itemStack) {
        return setItem(itemStack, itemStack.getCount());
    }
    public boolean setItem(ItemStack itemStack, int quantity) {
        if (!canContainItem(itemStack)) {return false;}
        itemType = itemStack.getItem();
        items.put(getSingleItemWrapper(itemStack), quantity);
        return true;
    }

    public Integer get(ItemStack itemStack) {
        ItemWrapper toReturn = getSingleItemWrapper(itemStack);
        return items.getOrDefault(toReturn, 0);
    }

    public ItemStack popItem(ItemStack itemStack) {
        if (containsKey(itemStack)) {
            popItem(itemStack, get(itemStack));
        }
        return null;
    }

    public ItemStack popItem(ItemStack itemStack, int maxQuantity) {
        int actualQuantity = Math.min(maxQuantity, items.getOrDefault(getSingleItemWrapper(itemStack), 0));
        if (actualQuantity==0) {return null;}
        else {
            ItemWrapper wrapper = getSingleItemWrapper(itemStack);
            items.merge(wrapper, -actualQuantity, Integer::sum);

            if (items.getOrDefault(wrapper,0) <= 0) {
                items.remove(wrapper);
                if (isEmpty()) {itemType = null;}
            }

            ItemStack toReturn = wrapper.getItemStack();
            toReturn.setCount(actualQuantity);
            return toReturn;
        }
    }

    public NonNullList<ItemStack> toNonNullList() {
        NonNullList<ItemStack> itemList = NonNullList.createWithCapacity(this.items.size());

        itemList.clear();
        for (Map.Entry<ItemWrapper, Integer> entry : items.entrySet()) {
            if (canContainItem(entry.getKey())) {
                itemList.add(makeItemStackOfCount(entry.getKey().getItemStack(), entry.getValue()));
            } else {
                itemList.add(ItemStack.EMPTY);
            }
        }

        return itemList;
    }

    public List<ItemStack> toItemStackList() {
        List<ItemStack> itemList = new ArrayList<>(this.items.size());

        for (Map.Entry<ItemWrapper, Integer> entry : items.entrySet()) {
            ItemStack stack;
            if (canContainItem(entry.getKey())) {
                stack = makeItemStackOfCount(entry.getKey().getItemStack(), entry.getValue());
            } else {
                stack = ItemStack.EMPTY;
            }
            itemList.add(stack);
        }

        return itemList;
    }
    public static SingleItemHolder fromNonNullList(NonNullList<ItemStack> list) {
        SingleItemHolder toReturn = new SingleItemHolder();

        for (ItemStack itemStack : list) {
            if (toReturn.canContainItem(itemStack)) {
                toReturn.putItem(itemStack);
            }
        }

        return toReturn;
    }



    public static ItemStack makeItemStackOfCount(ItemStack itemStack, int count) {
        ItemStack copy = itemStack.copy();
        copy.setCount(count);
        return copy;
    }




    /*
    Collections Framework methods
     */


    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of elements in this collection
     */
    public int size() {
        return items.size();
    }

    /**
     * Returns {@code true} if this collection contains no elements.
     *
     * @return {@code true} if this collection contains no elements
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns {@code true} if this collection contains the specified element.
     * More formally, returns {@code true} if and only if this collection
     * contains at least one element {@code e} such that
     * {@code Objects.equals(o, e)}.
     *
     * @param o element whose presence in this collection is to be tested
     * @return {@code true} if this collection contains the specified
     * element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection
     *                              (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements
     *                              (<a href="{@docRoot}/java.base/java/util/Collection.html#optional-restrictions">optional</a>)
     */
    public boolean containsKey(Object o) {
        if (o instanceof ItemStack itemStack) {return items.containsKey(getSingleItemWrapper(itemStack));}
        else {return false;}
    }


    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).
     * <br>
     * More formally, removes an element {@code e} such that {@code Objects.equals(o, e)},
     * <br>
     * if this collection contains one or more such elements.
     * <br>
     * Returns {@code true} if this collection contained the specified element
     * (or equivalently, if this collection changed as a result of the call).
     *
     * @param itemStack element to be removed from this collection, if present
     * @return {@code true} if an element was removed as a result of this call
     */
    public boolean remove(@NotNull ItemStack itemStack) {
        boolean flag = (items.remove(getSingleItemWrapper(itemStack))!=null);
        if (isEmpty()) {itemType = null;}
        return flag;
    }

    public boolean remove(@NotNull ItemWrapper itemWrapper) {
        boolean flag = (items.remove(itemWrapper)!=null);
        if (isEmpty()) {itemType = null;}
        return flag;
    }

    public boolean addAll(SingleItemHolder itemHolder) {
        NonNullList<ItemStack> toAdd = itemHolder.toNonNullList();
        if (toAdd.isEmpty()) {return false;}
        this.addAll(toAdd);
        return true;
    }

    /**
     * Adds all the elements in the specified collection to this collection
     * <br>
     * The behavior of this operation is undefined if the specified collection
     * is modified while the operation is in progress.
     *
     * @param c collection containing elements to be added to this collection
     * @return {@code true} if this collection changed as a result of the call
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #putItem(ItemStack)
     */
    public boolean addAll(@NotNull Collection<? extends ItemStack> c) {
        boolean flag = true;
        for (ItemStack itemStack : c) {
            if (putItem(itemStack)) {
                continue;
            }

            flag = false;
        }
        return flag;
    }



    /**
     * Removes all the elements from this collection.
     * The collection will be empty after this method returns.
     */
    public void clear() {
        this.items = new HashMap<>();
        this.itemType = null;
    }


    /**
     * Performs the given action for each entry in this map until all entries
     * have been processed or the action throws an exception.   Unless
     * otherwise specified by the implementing class, actions are performed in
     * the order of entry set iteration (if an iteration order is specified.)
     * Exceptions thrown by the action are relayed to the caller.
     * <br><br>
     * The default implementation makes no guarantees about synchronization
     * or atomicity properties of this method. Any implementation providing
     * atomicity guarantees must override this method and document its
     * concurrency properties.
     *
     * @param action The action to be performed for each entry
     * @throws NullPointerException if the specified action is null
     * @throws ConcurrentModificationException if an entry is found to be
     * removed during iteration
     * @since 1.8
     */
    public void forEach(BiConsumer<? super ItemWrapper, ? super Integer> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<ItemWrapper, Integer> entry : items.entrySet()) {
            ItemWrapper k;
            Integer v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k, v);
        }
    }
}
