package net.willo678.filingcabinet.container;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.willo678.filingcabinet.network.ClientToServerStoragePacket;
import net.willo678.filingcabinet.network.Networking;
import net.willo678.filingcabinet.network.ServerToClientStoragePacket;
import net.willo678.filingcabinet.screen.FilingCabinetMenu;
import net.willo678.filingcabinet.util.ItemWrapper;
import net.willo678.filingcabinet.util.SingleItemHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static net.willo678.filingcabinet.util.ItemWrapper.getSingleItemWrapper;

public class CabinetSyncManager {
    private static final int MAX_PACKET_SIZE = 32000;
    private final Object2IntMap<ItemWrapper> idMap = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<ItemWrapper> idMap2 = new Int2ObjectArrayMap<>();
    private final SingleItemHolder items = new SingleItemHolder();
    private final SingleItemHolder itemList = new SingleItemHolder();
    private int lastId = 1;
    private final FriendlyByteBuf workBuf = new FriendlyByteBuf(Unpooled.buffer());

    private void writeStack(FriendlyByteBuf buf, ItemStack stack) {
        Item item = stack.getItem();
        CompoundTag compoundTag = getSyncTag(stack);
        byte flags = (byte) ((stack.getCount() == 0 ? 1 : 0) | (compoundTag!=null ? 2 : 0));
        boolean wr = true;
        int id = idMap.getInt(stack);
        if (id!=0) {
            flags |= 4;
            wr = false;
        }

        buf.writeByte(flags);
        buf.writeVarInt(idMap.computeIfAbsent(getSingleItemWrapper(stack), s-> {
            int i = lastId++;
            idMap2.put(i, (ItemWrapper) s);
            return i;
        }));

        if (wr) {writeItemId(buf, item);}
        if (stack.getCount()!=0) {buf.writeVarLong(stack.getCount());}
        if (wr && compoundTag!=null) {buf.writeNbt(compoundTag);}
    }


    private ItemStack read(FriendlyByteBuf buf) {
        byte flags = buf.readByte();
        int id = buf.readVarInt();
        boolean rd = (flags & 4) == 0;
        ItemStack stack;
        if(rd) {
            stack = new ItemStack(readItemId(buf));
        } else {
            stack = idMap2.get(id).getItemStack().copy();
        }
        long count = (flags & 1) != 0 ? 0 : buf.readVarLong();
        stack.setCount((int) count);
        if(rd && (flags & 2) != 0) {
            stack.setTag(buf.readNbt());
        }
        idMap.put(getSingleItemWrapper(stack), id);
        idMap2.put(id, getSingleItemWrapper(stack));
        return stack;
    }

    public void update(SingleItemHolder items, ServerPlayer player, Consumer<CompoundTag> extraSync) {
        List<ItemStack> toWrite = new ArrayList<>();
        Set<ItemWrapper> found = new HashSet<>();
        items.forEach((s, c) -> {
            long pc = this.items.get(s.getItemStack());
            if(pc != 0L)found.add(s);
            if(pc != c) {
                ItemStack stack = s.getItemStack();
                stack.setCount(c);
                toWrite.add(stack);
            }
        });
        this.items.forEach((s, c) -> {
            if(!found.contains(s)) {
                ItemStack stack = s.getItemStack();
                stack.setCount(0);
                toWrite.add(stack);
            }
        });
        this.items.clear();
        this.items.addAll(items);
        if(!toWrite.isEmpty()) {
            workBuf.writerIndex(0);
            int j = 0;
            for (int i = 0; i < toWrite.size(); i++, j++) {
                ItemStack stack = toWrite.get(i);
                int li = workBuf.writerIndex();
                writeStack(workBuf, stack);
                int s = workBuf.writerIndex();
                if((s > MAX_PACKET_SIZE || j > 32000) && j > 1) {
                    CompoundTag t = writeBuf("d", workBuf, li);
                    t.putShort("l", (short) j);
                    Networking.sendToPlayerClient(new ServerToClientStoragePacket(t), player);
                    j = 0;
                    workBuf.writerIndex(0);
                    workBuf.writeBytes(workBuf, li, s - li);
                }
            }
            if(j > 0 || extraSync != null) {
                CompoundTag t;
                if(j > 0) {
                    t = writeBuf("d", workBuf, workBuf.writerIndex());
                    t.putShort("l", (short) j);
                } else t = new CompoundTag();
                if(extraSync != null)extraSync.accept(t);
                Networking.sendToPlayerClient(new ServerToClientStoragePacket(t), player);
            }
        } else if(extraSync != null) {
            CompoundTag t = new CompoundTag();
            extraSync.accept(t);
            Networking.sendToPlayerClient(new ServerToClientStoragePacket(t), player);
        }
    }

    public boolean receiveUpdate(CompoundTag tag) {
        if(tag.contains("d")) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(tag.getByteArray("d")));
            List<ItemStack> in = new ArrayList<>();
            short len = tag.getShort("l");
            for (int i = 0; i < len; i++) {
                in.add(read(buf));
            }
            in.forEach(s -> {
                if(s.getCount() == 0) {
                    this.itemList.remove(s);
                } else {
                    this.itemList.setItem(s);
                }
            });
            return true;
        }
        return false;
    }

    public void sendClientInteract(ItemStack intStack, FilingCabinetMenu.SlotAction action, boolean pullOne) {
        CompoundTag interactTag = new CompoundTag();
        interactTag.putBoolean("pullOne", pullOne);
        interactTag.putInt("action", action.ordinal());
        if(intStack != null){
            interactTag.putInt("id", idMap.getInt(getSingleItemWrapper(intStack)));
            interactTag.putLong("qty",  intStack.getCount());
        }
        CompoundTag dataTag = new CompoundTag();
        dataTag.put("interaction", interactTag);
        Networking.sendToServer(new ClientToServerStoragePacket(dataTag));
    }

    private CompoundTag writeBuf(String id, FriendlyByteBuf buf, int len) {
        byte[] data = new byte[len];
        buf.getBytes(0, data);
        CompoundTag tag = new CompoundTag();
        tag.putByteArray(id, data);
        return tag;
    }

    public void receiveInteract(CompoundTag tag, FilingCabinetMenu handler) {
        if(!tag.contains("interaction"))
            return;

        CompoundTag interactTag = tag.getCompound("interaction");
        boolean pullOne = interactTag.getBoolean("pullOne");
        ItemStack stack = null;
        if(interactTag.contains("id")){
            stack = idMap2.get(interactTag.getInt("id")).getItemStack().copy();
            stack.setCount((int) interactTag.getLong("qty"));
        }
        FilingCabinetMenu.SlotAction action = FilingCabinetMenu.SlotAction.values()[interactTag.getInt("action")];
        handler.onInteract(stack, action, pullOne);
    }



    public static ResourceLocation getItemId(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static void writeItemId(FriendlyByteBuf buf, Item item) {
        buf.writeId(Registry.ITEM, item);
    }

    public static Item readItemId(FriendlyByteBuf buf) {
        return buf.readById(Registry.ITEM);
    }

    public static CompoundTag getSyncTag(ItemStack stack) {
        Item item = stack.getItem();
        CompoundTag compoundTag = null;
        if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
            compoundTag = stack.getShareTag();
        }
        return  compoundTag;
    }

    public List<ItemStack> getAsList() {
        return this.itemList.toItemStackList();
    }
}
