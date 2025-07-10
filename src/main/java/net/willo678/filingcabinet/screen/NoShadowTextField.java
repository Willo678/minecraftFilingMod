package net.willo678.filingcabinet.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.willo678.filingcabinet.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Function;

public class NoShadowTextField extends net.willo678.filingcabinet.screen.EditBox {

    public Function<String, Void> onClear;


    public NoShadowTextField(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }
    public NoShadowTextField(Font font, int x, int y, int width, int height, @Nullable EditBox editBox, Component message) {
        super(font, x, y, width, height, editBox, message);
    }

    @Override
    protected void onValueChange(String pNewText) {
        super.onValueChange(pNewText);
        Constants.log("CursorPos: "+this.cursorPos + " / DisplayPos: "+this.displayPos + " / HighlightPos: "+this.highlightPos);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        if (this.isVisible()) {
            int colour = this.isEditable ? this.textColor : this.textColorUneditable;
            int displayCursorPos = this.cursorPos - this.displayPos;
            int displayHighlightPos = this.highlightPos - this.displayPos;

            String toDisplay = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean cursorWithinRange = (displayCursorPos>=0) && (displayCursorPos<=toDisplay.length());
            boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && cursorWithinRange;
            int l = this.bordered ? this.x + 4 : this.x;
            int i1 = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int j1 = l;
            if (displayHighlightPos > toDisplay.length()) {
                displayHighlightPos = toDisplay.length();
            }

            if (!toDisplay.isEmpty()) {
                String s1 = cursorWithinRange ? toDisplay.substring(0, displayCursorPos) : toDisplay;
                j1 = this.font.draw(poseStack, s1, (float) l, (float) i1, -8355712);
            }

            boolean flag2 = this.cursorPos < this.value.length() || this.value.length() >= 32;
            int k1 = j1;
            if (!cursorWithinRange) {
                k1 = displayCursorPos > 0 ? l + this.width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!toDisplay.isEmpty() && cursorWithinRange && displayCursorPos < toDisplay.length()) {
                this.font.draw(poseStack, toDisplay.substring(displayCursorPos), (float) j1, (float) i1, colour);
            }

            if (!flag2 && this.suggestion != null) {
                this.font.draw(poseStack, this.suggestion, (float) (k1 - 1), (float) i1, -8355712);
            }

            if (flag1) {
                if (flag2) {
                    GuiComponent.fill(poseStack, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
                } else {
                    this.font.draw(poseStack, "_", (float) k1, (float) i1, colour);
                }
            }
        }

        super.renderButton(poseStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double clickedX, double clickedY, int mouseButton) { // 0 for primary, 1 for secondary
        if (!this.isVisible()) {
            return false;
        } else {
            boolean clickedThis = clickedX >= (double) this.x && clickedX < (double) (this.x + this.width) && clickedY >= (double) this.y && clickedY < (double) (this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(clickedThis);
            }

            if (this.isFocused() && clickedThis && mouseButton == 0) {
                int i = Mth.floor(clickedX) - this.x;
                if (this.bordered) {
                    i -= 4;
                }

                String s = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(s, i).length() + this.displayPos);
                return true;
            } else if (this.isFocused() && mouseButton == 1) {
                if (this.value.isEmpty())
                    return clickedThis;


                if (onClear != null)
                    onClear.apply("");
                setValue("");
                return clickedThis;
            } else {
                return false;
            }
        }
    }

    public void setY(int i) {y=i;}
    public int getX() {return x;}
    public int getY() {return y;}

}
