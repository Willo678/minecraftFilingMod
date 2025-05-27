package net.willo678.filingcabinet.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.willo678.filingcabinet.FilingMod;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FilingMod.MODID);




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
