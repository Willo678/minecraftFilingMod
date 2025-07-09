package net.willo678.filingcabinet.util;

import com.mojang.logging.LogUtils;
import net.willo678.filingcabinet.container.StoredItemStack;
import org.slf4j.Logger;

import java.util.Comparator;

public class Constants {

    public static final String MODID = "willos_filings";

    public static final Logger LOGGER = LogUtils.getLogger();
    public static void log(String msg) {
        LOGGER.debug(msg);
    }


    public static ChestType FILING_CABINET = new ChestType(111, 9, 6,198, 219, 256, 256);

    public static Comparator<StoredItemStack> FILING_COMPARATOR = (o1, o2) -> {
        int result = (int) (o2.getQuantity()-o1.getQuantity());
        if (result==0) {
            return o1.getDisplayName().compareTo(o2.getDisplayName());
        }
        return result;
    };


}
