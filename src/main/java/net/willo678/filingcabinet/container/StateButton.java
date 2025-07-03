package net.willo678.filingcabinet.container;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

public class StateButton extends BaseButton {

    public ResourceLocation texture;
    public int tile;
    public int state;
    public int texX = 0;
    public int texY = 0;
    public int imageWidth;
    public int imageHeight;


    public StateButton(int x, int y, int width, int height, int imageWidth, int imageHeight, int tile, ResourceLocation texture, OnPress pressable) {
        super(x, y, width, height, null, pressable);
        this.tile = tile;
        this.texture = texture;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void renderButton(PoseStack st, int mouseX, int mouseY, float pt) {
        if (this.visible) {
            int x = getX();
            int y = getY();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, texture);
            this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
            //int i = this.getYImage(this.isHovered);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            blit(st, x, y, texX + state * width, texY + tile * height, this.width, this.height, imageWidth, imageHeight);
        }
    }
}
