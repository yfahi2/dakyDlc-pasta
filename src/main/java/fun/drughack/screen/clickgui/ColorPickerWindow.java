package fun.drughack.screen.clickgui;

import fun.drughack.screen.clickgui.components.impl.ColorSettings;
import fun.drughack.utils.render.Render2D;
import fun.drughack.utils.render.fonts.Fonts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static fun.drughack.utils.Wrapper.mc;

public class ColorPickerWindow extends Screen {


    // У У У У пасаси да я богатый уебок [Умные мысли морген штерн]


    private final ColorSettings colorSetting;

    private float centerX, centerY;
    private final float width = 250, height = 250;
    private final int squareSize = 150;

    private boolean draggingSquare = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    private BufferedImage satBrightImage;
    private BufferedImage hueBarImage;
    private BufferedImage alphaBarImage;


    private float animSatPos, animBrightPos, animHuePos, animAlphaPos;

    public ColorPickerWindow(ColorSettings setting) {
        super(Text.of("Color Picker"));
        this.colorSetting = setting;

        createHueBarImage();
        createAlphaBarImage();
        updateSatBrightImage();
    }

    @Override
    protected void init() {
        centerX = (mc.getWindow().getScaledWidth() - width) / 2f;
        centerY = (mc.getWindow().getScaledHeight() - height) / 2f;


        animHuePos = colorSetting.getHue() * squareSize + centerX;
        animSatPos = colorSetting.getSaturation() * squareSize + centerX;
        animBrightPos = (1f - colorSetting.getBrightness()) * squareSize + centerY;
        animAlphaPos = colorSetting.getAlpha() * squareSize + centerX;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float hueBarY = centerY + squareSize + 10;
        float alphaBarY = hueBarY + 20;


        Render2D.drawRoundedRect(context.getMatrices(), centerX - 5, centerY - 5, width + 10, height + 10, 5, new Color(34, 34, 34));


        Render2D.drawBufTexture(context.getMatrices(), centerX, centerY, squareSize, squareSize, 0, satBrightImage, Color.WHITE);


        Render2D.drawBufTexture(context.getMatrices(), centerX, hueBarY, squareSize, 10, 0, hueBarImage, Color.WHITE);


        Render2D.drawBufTexture(context.getMatrices(), centerX, alphaBarY, squareSize, 10, 0, alphaBarImage, Color.WHITE);

        animHuePos = lerp(animHuePos, colorSetting.getHue() * squareSize + centerX, 0.3f);
        animSatPos = lerp(animSatPos, colorSetting.getSaturation() * squareSize + centerX, 0.3f);
        animBrightPos = lerp(animBrightPos, (1f - colorSetting.getBrightness()) * squareSize + centerY, 0.3f);
        animAlphaPos = lerp(animAlphaPos, colorSetting.getAlpha() * squareSize + centerX, 0.3f);


        Render2D.drawRoundedRect(context.getMatrices(), animSatPos - 3, animBrightPos - 3, 6, 6, 3, Color.WHITE);
        Render2D.drawRoundedRect(context.getMatrices(), animHuePos - 3, hueBarY - 2, 6, 14, 3, Color.WHITE);
        Render2D.drawRoundedRect(context.getMatrices(), animAlphaPos - 3, alphaBarY - 2, 6, 14, 3, Color.WHITE);


        Color c = new Color(colorSetting.getColorWithAlpha(), true);
        String rgbText = String.format("RGB: %d, %d, %d", c.getRed(), c.getGreen(), c.getBlue());
        String hexText = String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());

        Render2D.drawFont(context.getMatrices(), Fonts.BOLD.getFont(10), rgbText, centerX, alphaBarY + 25, Color.WHITE);
        Render2D.drawFont(context.getMatrices(), Fonts.BOLD.getFont(10), hexText, centerX, alphaBarY + 40, Color.WHITE);


        float buttonWidth = 50, buttonHeight = 12;
        float buttonX = centerX + 150, buttonY = alphaBarY + 25;


        Render2D.drawRoundedRect(context.getMatrices(), buttonX, buttonY, buttonWidth, buttonHeight, 3, new Color(50, 50, 50));
        Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(7), "Copy RGB", buttonX + 5, buttonY + 2, Color.WHITE);


        Render2D.drawRoundedRect(context.getMatrices(), buttonX, buttonY + 15, buttonWidth, buttonHeight, 3, new Color(50, 50, 50));
        Render2D.drawFont(context.getMatrices(), Fonts.REGULAR.getFont(7), "Copy HEX", buttonX + 5, buttonY + 17, Color.WHITE);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float hueBarY = centerY + squareSize + 10;
        float alphaBarY = hueBarY + 20;

        if (button == 0) {
            draggingSquare = isHovered(centerX, centerY, squareSize, squareSize, mouseX, mouseY);
            draggingHue = isHovered(centerX, hueBarY, squareSize, 10, mouseX, mouseY);
            draggingAlpha = isHovered(centerX, alphaBarY, squareSize, 10, mouseX, mouseY);

            Color c = new Color(colorSetting.getColorWithAlpha(), true);
            float buttonX = centerX + 150, buttonY = alphaBarY + 25;
            float buttonWidth = 50, buttonHeight = 12;


            if (isHovered(buttonX, buttonY, buttonWidth, buttonHeight, mouseX, mouseY)) {
                copyToClipboard(String.format("%d, %d, %d", c.getRed(), c.getGreen(), c.getBlue()));
            }


            if (isHovered(buttonX, buttonY + 15, buttonWidth, buttonHeight, mouseX, mouseY)) {
                copyToClipboard(String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue()));
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            draggingSquare = false;
            draggingHue = false;
            draggingAlpha = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();

        boolean colorChanged = false;

        if (draggingSquare) colorChanged |= setSaturationBrightness((float)(mouseX - centerX) / squareSize, 1f - (float)(mouseY - centerY) / squareSize);
        if (draggingHue) colorChanged |= setHue((float)(mouseX - centerX) / squareSize);
        if (draggingAlpha) colorChanged |= setAlpha((float)(mouseX - centerX) / squareSize);

        if (colorChanged) updateAlphaBarImage();
    }

    private boolean setSaturationBrightness(float sat, float bright) {
        boolean changed = false;
        float clampedSat = MathHelper.clamp(sat, 0f, 1f);
        float clampedBright = MathHelper.clamp(bright, 0f, 1f);
        if (colorSetting.getSaturation() != clampedSat || colorSetting.getBrightness() != clampedBright) {
            colorSetting.setSaturation(clampedSat);
            colorSetting.setBrightness(clampedBright);
            changed = true;
        }
        return changed;
    }

    private boolean setHue(float hue) {
        float clampedHue = MathHelper.clamp(hue, 0f, 1f);
        if (colorSetting.getHue() != clampedHue) {
            colorSetting.setHue(clampedHue);
            updateSatBrightImage();
            return true;
        }
        return false;
    }

    private boolean setAlpha(float alpha) {
        float clampedAlpha = MathHelper.clamp(alpha, 0f, 1f);
        if (colorSetting.getAlpha() != clampedAlpha) {
            colorSetting.setAlpha(clampedAlpha);
            return true;
        }
        return false;
    }

    private boolean isHovered(float x, float y, float w, float h, double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void updateSatBrightImage() {
        satBrightImage = new BufferedImage(squareSize, squareSize, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < squareSize; i++) {
            for (int j = 0; j < squareSize; j++) {
                float s = (float)i / squareSize;
                float b = 1f - (float)j / squareSize;
                int color = Color.HSBtoRGB(colorSetting.getHue(), s, b) | 0xFF000000;
                satBrightImage.setRGB(i, j, color);
            }
        }
    }

    private void createHueBarImage() {
        hueBarImage = new BufferedImage(squareSize, 10, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < squareSize; i++) {
            float h = (float)i / squareSize;
            int color = Color.HSBtoRGB(h, 1f, 1f) | 0xFF000000;
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
            float a = (float)i / squareSize;
            int color = (colorSetting.getColorWithAlpha() & 0x00FFFFFF) | ((int)(a * 255) << 24);
            for (int j = 0; j < 10; j++) alphaBarImage.setRGB(i, j, color);
        }
    }

    private float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private void copyToClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}