package net.willo678.filingcabinet.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.willo678.filingcabinet.container.StorageSlot;
import net.willo678.filingcabinet.container.StoredItemStack;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;
import net.willo678.filingcabinet.util.NumberFormatUtil;
import org.lwjgl.glfw.GLFW;

import static net.willo678.filingcabinet.screen.FilingCabinetMenu.SlotAction.*;

public class FilingCabinetScreen extends AbstractContainerScreen<FilingCabinetMenu> implements MenuAccess<FilingCabinetMenu> {

    protected static Minecraft mc = Minecraft.getInstance();

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/filing_cabinet_menu.png");

    private static final ChestType chestType = FilingCabinetMenu.chestType;

    private FilingCabinetMenu parent;

    private final int textureXSize;
    private final int textureYSize;

    protected int slotIDUnderMouse = -1;


    public FilingCabinetScreen(FilingCabinetMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);

        this.parent = menu;

        this.imageWidth = chestType.xSize;
        this.imageHeight = chestType.ySize;

        this.textureXSize = chestType.xTextureSize;
        this.textureYSize = chestType.yTextureSize;

        this.passEvents = false;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title, 8.0F, 6.0F, 4210752);

        this.font.draw(matrixStack, this.playerInventoryTitle, 8.0F, (float) (this.imageHeight - 92), 4210752);


        matrixStack.pushPose();
        slotIDUnderMouse = drawSlots(matrixStack, mouseX, mouseY);
        matrixStack.popPose();


        //super.renderLabels(poseStack, mouseX, mouseY);
    }


    protected int drawSlots(PoseStack poseStack, int mouseX, int mouseY) {
        FilingCabinetMenu menu = getMenu();
        int slotHover = -1;

        for (int i=0; i<menu.storageSlotList.size(); i++) {
            if (drawSlot(poseStack, menu.storageSlotList.get(i), mouseX, mouseY)) {
                slotHover = i;
            }
        }

        return slotHover;
    }

    protected boolean drawSlot(PoseStack poseStack, StorageSlot slot, int mouseX, int mouseY) {
        if (slot.stack != null) {
            this.setBlitOffset(100);
            this.itemRenderer.blitOffset = 100.0F;

            ItemStack stack = slot.stack.getStack().copy().split(1);
            int i = slot.xDisplayPosition, j = slot.yDisplayPosition;

            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, stack, i, j, 0);
            this.itemRenderer.renderGuiItemDecorations(this.font, stack, i, j, null);

            drawStackSize(poseStack, getFont(), slot.stack.getQuantity(), i, j);

            this.itemRenderer.blitOffset = 0.0F;
            this.setBlitOffset(0);
        }

        if (mouseX >= getGuiLeft() + slot.xDisplayPosition - 1 && mouseY >= getGuiTop() + slot.yDisplayPosition - 1 && mouseX < getGuiLeft() + slot.xDisplayPosition + 17 && mouseY < getGuiTop() + slot.yDisplayPosition + 17) {
            int l = slot.xDisplayPosition;
            int t = slot.yDisplayPosition;
            renderSlotHighlight(poseStack, l, t, this.getBlitOffset());
            return true;
        }
        return false;
    }


    private void drawStackSize(PoseStack st, Font fr, long size, int x, int y) {
        float scaleFactor = 0.6f;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        String stackSize = NumberFormatUtil.formatNumber(size);
        st.pushPose();
        st.scale(scaleFactor, scaleFactor, scaleFactor);
        st.translate(0, 0, 450);
        float inverseScaleFactor = 1.0f / scaleFactor;
        int X = (int) (((float) x + 0 + 16.0f - fr.width(stackSize) * scaleFactor) * inverseScaleFactor);
        int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
        fr.drawShadow(st, stackSize, X, Y, 16777215);
        st.popPose();
        RenderSystem.enableDepthTest();
    }






    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack poseStack, float mouseX, int mouseY, int partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight, this.textureXSize, this.textureYSize);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        handleMouseScroll(delta<0);

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void handleMouseScroll(boolean isPositive) {
        if (isPositive) {
            parent.incrementScrollProgress();
        } else {
            parent.decrementScrollProgress();
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        Constants.log("Click: {"+mouseX+", "+mouseY+"} + HoveredSlot: "+slotIDUnderMouse);

        if (slotIDUnderMouse > -1) {
            if (isPullOne(mouseButton)) {
                if (slotIdContainsAny() && menu.getCarried().isEmpty()) {
                    storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, PULL_ONE, isTransferOne(mouseButton));
                    return true;
                }
            } else if (pullHalf(mouseButton)) {
                if (!menu.getCarried().isEmpty()) {
                    storageSlotClick(null, PUSH_ONE, false);
                } else if (slotIdContainsAny()) {
                    storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, hasControlDown() ? GET_QUARTER : GET_HALF, false);
                    return true;
                }
            } else if (pullNormal(mouseButton)) {
                if (!menu.getCarried().isEmpty()) {
                    storageSlotClick(null, PULL_OR_PUSH_STACK, false);
                }
                else if (slotIdContainsAny()) {
                    storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, hasShiftDown() ? SHIFT_PULL : PULL_OR_PUSH_STACK, false);
                    return true;
                }
            }
        } else if (GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_RELEASE) {
            storageSlotClick(null, SPACE_CLICK, false);
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void storageSlotClick(StoredItemStack slotStack, FilingCabinetMenu.SlotAction action, boolean pullOne) {
        Constants.log("SlotStack: "+slotStack+" / Action:"+action.name());
        menu.onInteract(slotStack, action, pullOne);
    }

    protected boolean slotIdContainsAny() {
        StoredItemStack stack = getMenu().getSlotByID(slotIDUnderMouse).stack;
        return stack!=null && stack.getQuantity() > 0;
    }

    public boolean isPullOne(int mouseButton) {
        return mouseButton == 1 && hasShiftDown();
    }

    public boolean isTransferOne(int mouseButton) {
        return hasShiftDown() && hasControlDown();
    }

    public boolean pullHalf(int mouseButton) {
        return mouseButton == 1;
    }

    public boolean pullNormal(int mouseButton) {
        return mouseButton == 0;
    }




    public Font getFont() {
        return this.font;
    }
}
