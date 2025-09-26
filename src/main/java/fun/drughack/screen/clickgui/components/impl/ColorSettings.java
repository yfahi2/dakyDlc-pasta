package fun.drughack.screen.clickgui.components.impl;

import fun.drughack.modules.settings.Setting;
import fun.drughack.utils.math.MathUtils;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.function.Supplier;

import static fun.drughack.utils.render.ColorUti.*;
import static net.minecraft.util.math.ColorHelper.*;

@Getter
@Setter
public class ColorSettings extends Setting {

    private float hue = 0f;
    private float saturation = 1f;
    private float brightness = 1f;
    private float alpha = 1f;

    private int[] presets;

    public ColorSettings(String name, String description) {
        super(name, description);
    }

    public ColorSettings value(int color) {
        return setColor(color);
    }

    public ColorSettings presets(int... presets) {
        this.presets = presets;
        return this;
    }

    public ColorSettings visible(Supplier<Boolean> visible) {
        setVisible(visible);
        return this;
    }

    public int getColor() {
        return (getColorWithAlpha() & 0x00FFFFFF) | (Math.round(alpha * 255) << 24);
    }

    public int getColorWithAlpha() {
        return Color.HSBtoRGB(hue, saturation, brightness);
    }

    public ColorSettings setColor(int color) {
        float[] hsb = Color.RGBtoHSB(
                getRed(color),
                getGreen(color),
                getBlue(color),
                null
        );

        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = MathUtils.getAlpha(color) / 255f;

        return this;
    }

    public int[] getGradientColors(int steps) {
        int[] colors = new int[steps];
        float originalBrightness = brightness;
        float originalSaturation = saturation;

        for (int i = 0; i < steps; i++) {
            float fraction = (float) i / (steps - 1);
            brightness = 0.5f + (0.5f * fraction);
            saturation = originalSaturation * (0.9f + 0.1f * fraction);
            colors[i] = getColor();
        }

        brightness = originalBrightness;
        saturation = originalSaturation;

        return colors;
    }
    public Color getAwtColor() {
        int c = getColor(); // ARGB
        return new Color((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, (c >> 24) & 0xFF);
    }
    public int getAwtColorInt() {
        return getColor(); // Color конструктор сам понимает ARGB формат
    }
    public int interpolateColor(float progress) {
        int[] colors = getGradientColors(2);
        int color1 = colors[0];
        int color2 = colors[1];

        int r = (int) (((color1 >> 16) & 0xFF) + (((color2 >> 16) & 0xFF) - ((color1 >> 16) & 0xFF)) * progress);
        int g = (int) (((color1 >> 8) & 0xFF) + (((color2 >> 8) & 0xFF) - ((color1 >> 8) & 0xFF)) * progress);
        int b = (int) ((color1 & 0xFF) + ((color2 & 0xFF) - (color1 & 0xFF)) * progress);
        int a = (int) (((color1 >> 24) & 0xFF) + (((color2 >> 24) & 0xFF) - ((color1 >> 24) & 0xFF)) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int getDarkerColor(float factor) {
        return Color.HSBtoRGB(hue, saturation, Math.max(0, brightness * (1 - factor)));
    }

    public int getLighterColor(float factor) {
        return Color.HSBtoRGB(hue, saturation, Math.min(1, brightness + (1 - brightness) * factor));
    }
}
