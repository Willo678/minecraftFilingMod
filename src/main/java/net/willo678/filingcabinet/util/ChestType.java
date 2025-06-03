package net.willo678.filingcabinet.util;

public class ChestType {
    //Rows
    public final int ROWS;
    public final int ROW_LENGTH;
    public final int TOTAL_SLOTS;

    public final int DISPLAY_ROWS;
    public final int DISPLAY_TOTAL_SLOTS;


    //Sizes
    public final int xSize;
    public final int ySize;
    public final int xTextureSize;
    public final int yTextureSize;



    public ChestType(int rows, int row_length, int displayRows, int xSize, int ySize, int xTextureSize, int yTextureSize) {
        this.ROWS = rows; this.ROW_LENGTH = row_length;
        this.TOTAL_SLOTS = ROWS*ROW_LENGTH;

        DISPLAY_ROWS = displayRows;
        DISPLAY_TOTAL_SLOTS = DISPLAY_ROWS*row_length;

        this.xSize = xSize; this.ySize = ySize;
        this.xTextureSize = xTextureSize; this.yTextureSize = yTextureSize;
    }
}
