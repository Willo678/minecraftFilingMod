package net.willo678.filingcabinet.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.common.Mod;

public class CreativeTab {
    public static CreativeModeTab TAB = new CreativeModeTab("filing_tab") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.CREATIVE_TAB.get());
        }
    };
}
