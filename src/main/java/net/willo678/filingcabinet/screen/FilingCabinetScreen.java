package net.willo678.filingcabinet.screen;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.willo678.filingcabinet.container.StorageSlot;
import net.willo678.filingcabinet.container.StoredItemStack;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;
import net.willo678.filingcabinet.util.NumberFormatUtil;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.willo678.filingcabinet.container.CabinetSyncManager.getItemId;
import static net.willo678.filingcabinet.screen.FilingCabinetMenu.SlotAction.*;

public class FilingCabinetScreen extends AbstractContainerScreen<FilingCabinetMenu> implements MenuAccess<FilingCabinetMenu> {
    private static final LoadingCache<StoredItemStack, List<String>> tooltipCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build(new CacheLoader<>() {

        @Override
        public @NotNull List<String> load(@NotNull StoredItemStack key) {
            return key.getStack().getTooltipLines(Minecraft.getInstance().player, getTooltipFlag()).stream().map(Component::getString).collect(Collectors.toList());
        }

    });


    protected static Minecraft mc = Minecraft.getInstance();
    private final FilingCabinetMenu parent;
    private static final ChestType chestType = FilingCabinetMenu.chestType;


    // Texture related variables
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/filing_cabinet_menu.png");
    private final int textureXSize;
    private final int textureYSize;


    // Scrolling related variables
    protected static final ResourceLocation scrollBar
            = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/scrollbars.png");
    protected int scrollAmount = 0;
    protected float scrollMaxAmount = 0;
    protected boolean scrollingEnabled;


    // Searching related variables
    protected NoShadowTextField searchField;
    protected String searchLast = "";
    private boolean refreshItemList = true;


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
        clearWidgets();
        super.init();

        this.searchField = new NoShadowTextField(getFont(), this.leftPos+86, this.topPos+6, 80, getFont().lineHeight+1, Component.translatable("narrator.willos_filings.search"));
        this.searchField.setMaxLength(100);
        this.searchField.setBordered(false);
        this.searchField.setVisible(true);
        this.searchField.setValue(searchLast);

        addRenderableWidget(searchField);

        refreshItemList = true;

        updateSearch();
    }


    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(poseStack, mouseX, mouseY);


        scrollAmount = Math.min(scrollAmount, (int)scrollMaxAmount);
        scrollMaxAmount = (int) Math.ceil(Math.max(0, getMenu().sortedItemList.size()-chestType.DISPLAY_TOTAL_SLOTS) / 9.0);
        scrollingEnabled = (scrollMaxAmount>0);
        parent.scrollTo(scrollAmount);
        renderScrollbar(poseStack, mouseX, mouseY, partialTicks);
    }

    @SuppressWarnings("unused")
    protected void renderScrollbar(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, scrollBar);
        int scrollTop = this.topPos+18;
        int scrollMaxDistance = 91;
        int scrollDistance = Math.min(scrollMaxDistance , (int)(scrollMaxDistance * (scrollAmount/(scrollMaxAmount==0 ? 1.0 : scrollMaxAmount))));
        blit(poseStack, (this.leftPos+178), (scrollTop + scrollDistance), (scrollingEnabled ? 0 : 12), 0, 12, 15, 24, 15);

    }


    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float mouseX, int mouseY, int partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, this.imageWidth, this.imageHeight, this.textureXSize, this.textureYSize);
    }


    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, 8.0F, 6.0F, 4210752);
        this.font.draw(poseStack, this.playerInventoryTitle, 8.0F, (float) (this.imageHeight - 92), 4210752);

        poseStack.pushPose();
        slotIDUnderMouse = drawSlots(poseStack, mouseX, mouseY);
        poseStack.popPose();

        if (menu.getCarried().isEmpty() && slotIDUnderMouse!=-1) {
            StorageSlot slot = getMenu().storageSlotList.get(slotIDUnderMouse);
            if (slot.stack!=null) {
                renderTooltip(poseStack, slot.stack.getActualStack(), (mouseX-getGuiLeft()), mouseY);
            }
        }
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

            this.itemRenderer.renderAndDecorateItem(Objects.requireNonNull(mc.player), stack, i, j, 0);
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
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        handleMouseScroll(delta>0);

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    public void handleMouseScroll(boolean isPositive) {
        if (isPositive) {
            if (scrollAmount>0) {scrollAmount--;}
        } else {
            if (scrollAmount< scrollMaxAmount) {scrollAmount++;}
        }
        scrollAmount = Math.min(scrollAmount, (int) scrollMaxAmount);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {


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
        menu.sync.sendClientInteract(slotStack, action, pullOne);
    }

    protected boolean slotIdContainsAny() {
        StoredItemStack stack = getMenu().getSlotByID(slotIDUnderMouse).stack;
        return stack!=null && stack.getQuantity() > 0;
    }

    public boolean isPullOne(int mouseButton) {
        return mouseButton == 1 && hasShiftDown();
    }

    @SuppressWarnings("unused")
    public boolean isTransferOne(int mouseButton) {
        return hasShiftDown() && hasControlDown();
    }

    public boolean pullHalf(int mouseButton) {
        return mouseButton == 1;
    }

    public boolean pullNormal(int mouseButton) {
        return mouseButton == 0;
    }


    @Override
    protected void containerTick() {
        updateSearch();
    }

    private void updateSearch() {
        String searchString = searchField.getValue().trim();

        searchField.setSuggestion( (searchField.getValue().isEmpty()) ? Component.translatable("narrator.willos_filings.search").getString() : "");


        if (refreshItemList || !searchLast.equals(searchString)) {
            getMenu().sortedItemList.clear();
            boolean searchMod = false;
            String search = searchString;

            if (searchString.startsWith("@")) {
                searchMod = true;
                search = searchString.substring(1);
            }

            Pattern m;
            try {
                m = Pattern.compile(search.toLowerCase(), Pattern.CASE_INSENSITIVE);
            } catch (Throwable ignore) {
                try {
                    m = Pattern.compile(Pattern.quote(search.toLowerCase()), Pattern.CASE_INSENSITIVE);
                } catch (Throwable __) {
                    return;
                }
            }

            try {
                for (int i=0; i<getMenu().itemList.size(); i++) {
                    StoredItemStack is = getMenu().itemList.get(i);
                    if (is!=null && is.getStack()!=null) {
                        String dspName = searchMod ? getItemId(is.getStack().getItem()).getNamespace() : is.getStack().getHoverName().getString();

                        if (m.matcher(dspName.toLowerCase()).find()) {
                            addStackToClientList(is);
                        } else {
                            for (String lp : tooltipCache.get(is)) {
                                if (m.matcher(lp).find()) {
                                    addStackToClientList(is);
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}

            getMenu().sortedItemList.sort(Constants.FILING_COMPARATOR);
            if (!searchLast.equals(searchString)) {
                this.scrollAmount = 0;
                getMenu().scrollTo(scrollAmount);
                CompoundTag nbt = new CompoundTag();
                nbt.putString("search", searchString);
                menu.sendMessage(nbt);

                onUpdateSearch(searchString);
            } else {
                getMenu().scrollTo(scrollAmount);
            }

            refreshItemList = false;
            this.searchLast = searchString;

        }
    }

    @SuppressWarnings("unused")
    private void onUpdateSearch(String searchString) {}

    private void addStackToClientList(StoredItemStack is) {
        getMenu().sortedItemList.add(is);
    }

    public static TooltipFlag getTooltipFlag() {
        return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
    }

    public Font getFont() {
        return this.font;
    }

    public void receive(CompoundTag tag) {
        menu.receiveClientNBTPacket(tag);
        refreshItemList = true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            this.onClose();
            return true;
        }
        return this.searchField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.searchField.canConsumeInput() || super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}
