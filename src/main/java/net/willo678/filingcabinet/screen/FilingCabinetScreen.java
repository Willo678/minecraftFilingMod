package net.willo678.filingcabinet.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.willo678.filingcabinet.util.ChestType;
import net.willo678.filingcabinet.util.Constants;

public class FilingCabinetScreen extends AbstractContainerScreen<FilingCabinetMenu> implements MenuAccess<FilingCabinetMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MODID, "textures/gui/filing_cabinet_menu.png");

    private static final ChestType chestType = FilingCabinetMenu.chestType;

    private final int textureXSize;
    private final int textureYSize;


    public FilingCabinetScreen(FilingCabinetMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);

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

        //super.renderLabels(poseStack, mouseX, mouseY);
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
}
