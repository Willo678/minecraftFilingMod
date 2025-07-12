package net.willo678.filingcabinet.util;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.Comparator;

public class Constants {

    public static final String MODID = "willos_filings";

    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();


    public static ChestType FILING_CABINET = new ChestType(111, 9, 6,198, 219, 256, 256);

    public static Comparator<ItemStack> FILING_COMPARATOR = (o1, o2) -> {
        int result = o2.getCount()-o1.getCount();
        if (result==0) {
            result = o1.getDisplayName().getString().compareTo(o2.getDisplayName().getString());
        }
        return result;
    };


}
