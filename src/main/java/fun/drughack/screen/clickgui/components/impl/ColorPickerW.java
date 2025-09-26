package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.screen.clickgui.components.Component;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.math.MathUtils;
import fun.drughack.utils.render.Render2D;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.awt.image.BufferedImage;



public class ColorPickerW implements Wrapper {

    private final ColorSettingsC colorSetting;

    private final int squareSize = 150;
    private boolean draggingSquare = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    private BufferedImage satBrightImage;
    private BufferedImage hueBarImage;
    private BufferedImage alphaBarImage;

    private float x, y;

    public ColorPickerW(ColorSettingsC setting) {
        this.colorSetting = setting;
        createHueBarImage();
        createAlphaBarImage();
        updateSatBrightImage();
    }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Фон
        Render2D.drawRoundedRect(context.getMatrices(), x, y, squareSize + 100, squareSize + 50, 5, new Color(34, 34, 34));

        // Sat/Brightness квадрат
        Render2D.drawBufTexture(context.getMatrices(), x + 5, y + 5, squareSize, squareSize, 0, satBrightImage, Color.WHITE);

        // Hue бар
        Render2D.drawBufTexture(context.getMatrices(), x + 5, y + squareSize + 10, squareSize, 10, 0, hueBarImage, Color.WHITE);

        // Alpha бар
        Render2D.drawBufTexture(context.getMatrices(), x + 5, y + squareSize + 25, squareSize, 10, 0, alphaBarImage, Color.WHITE);

        // Текущий цвет
        Color c = new Color(colorSetting.getColorWithAlpha(), true);
        Render2D.drawRoundedRect(context.getMatrices(), x + squareSize + 15, y + 5, 80, 40, 5, c);
    }

    public void tick(double mouseX, double mouseY) {
        float mx = (float) mouseX;
        float my = (float) mouseY;

        if (draggingSquare) {
            float sat = (mx - x - 5) / squareSize;
            float bright = 1f - (my - y - 5) / squareSize;
            colorSetting.setSaturation(MathHelper.clamp(sat, 0f, 1f));
            colorSetting.setBrightness(MathHelper.clamp(bright, 0f, 1f));
        }
        if (draggingHue) {
            float hue = (mx - x - 5) / squareSize;
            colorSetting.setHue(MathHelper.clamp(hue, 0f, 1f));
            updateSatBrightImage();
        }
        if (draggingAlpha) {
            float alpha = (mx - x - 5) / squareSize;
            colorSetting.setAlpha(MathHelper.clamp(alpha, 0f, 1f));
        }
        updateAlphaBarImage();
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        float mx = (float) mouseX;
        float my = (float) mouseY;

        draggingSquare = MathUtils.isHovered(x + 5, y + 5, squareSize, squareSize, mx, my);
        draggingHue = MathUtils.isHovered(x + 5, y + squareSize + 10, squareSize, 10, mx, my);
        draggingAlpha = MathUtils.isHovered(x + 5, y + squareSize + 25, squareSize, 10, mx, my);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        draggingSquare = false;
        draggingHue = false;
        draggingAlpha = false;
    }

    private void updateSatBrightImage() {
        satBrightImage = new BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < squareSize; i++) {
            for (int j = 0; j < squareSize; j++) {
                float s = (float) i / squareSize;
                float b = 1f - (float) j / squareSize;
                int color = Color.HSBtoRGB(colorSetting.getHue(), s, b) | 0xFF000000;
                satBrightImage.setRGB(i, j, color);
            }
        }
    }

    private void createHueBarImage() {
        hueBarImage = new BufferedImage(squareSize, 10, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < squareSize; i++) {
            int color = Color.HSBtoRGB((float) i / squareSize, 1f, 1f) | 0xFF000000;
            for (int j = 0; j < 10; j++) hueBarImage.setRGB(i, j, color);
        }
    }

    private void createAlphaBarImage() {
        alphaBarImage = new BufferedImage(squareSize, 10, BufferedImage.TYPE_INT_ARGB);
        updateAlphaBarImage();
    }

    private void updateAlphaBarImage() {
        alphaBarImage = new BufferedImage(squareSize, 10, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < squareSize; i++) {
            float a = (float) i / squareSize;
            int color = (colorSetting.getColorWithAlpha() & 0x00FFFFFF) | ((int) (a * 255) << 24);
            for (int j = 0; j < 10; j++) alphaBarImage.setRGB(i, j, color);
        }
    }
}