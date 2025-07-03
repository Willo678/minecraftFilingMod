package net.willo678.filingcabinet.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public class Constants {

    public static final String MODID = "willos_filings";

    public static final Logger LOGGER = LogUtils.getLogger();
    public static void log(String msg) {
        LOGGER.debug(msg);
    }


    public static ChestType FILING_CABINET = new ChestType(111, 9, 6,198, 219, 256, 256);




}
