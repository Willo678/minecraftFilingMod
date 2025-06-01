package net.willo678.filingcabinet.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CreativeTab {
    public static CreativeModeTab TAB = new CreativeModeTab("filing_tab") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ModItems.CREATIVE_TAB.get());
        }
    };
}
