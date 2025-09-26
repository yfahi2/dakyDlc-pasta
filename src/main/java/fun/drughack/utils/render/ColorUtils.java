package fun.drughack.utils.render;

import fun.drughack.DrugHack;
import fun.drughack.utils.Wrapper;
import fun.drughack.utils.math.MathUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;

@UtilityClass
public class ColorUtils implements Wrapper {

    /**
     * Считывает цвет с экрана по координатам (пикер цвета)
     */
    public Color picker(float x, float y) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(4);
        GL11.glReadPixels(
                (int) (x * mc.getWindow().getScaleFactor()),
                (int) ((mc.getWindow().getScaledHeight() - y) * mc.getWindow().getScaleFactor()),
                1, 1,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE, buffer
        );
        return new Color(buffer.get() & 0xFF, buffer.get() & 0xFF, buffer.get() & 0xFF);
    }

    /**
     * Преобразует цвет в формате ARGB (int) в массив [R, G, B, A] в долях от 1.0 (0.0 - 1.0)
     */
    public static float[] getRGBa(final int color) {
        return new float[]{
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }


    /**
     * Создаёт плавный градиентный цвет, зависящий от времени
     */
    public static int gradient2(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) - 180;
        angle = MathHelper.clamp(angle, 0, 180);
        float progress = angle / 180f;

        int color = interpolate2(start, end, progress);
        float[] hs = getRGBa(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    /**
     * Интерполяция между двумя цветами (ARGB)
     * Исправлено: теперь правильно создаёт цвет из компонент R, G, B, A
     */
    public static int interpolate2(int start, int end, float value) {
        float[] startColor = getRGBa(start);
        float[] endColor = getRGBa(end);

        int r = (int) MathUtils.interpolate2(startColor[0] * 255, endColor[0] * 255, value);
        int g = (int) MathUtils.interpolate2(startColor[1] * 255, endColor[1] * 255, value);
        int b = (int) MathUtils.interpolate2(startColor[2] * 255, endColor[2] * 255, value);
        int a = (int) MathUtils.interpolate2(startColor[3] * 255, endColor[3] * 255, value);

        // Ограничиваем значения от 0 до 255
        r = MathHelper.clamp(r, 0, 255);
        g = MathHelper.clamp(g, 0, 255);
        b = MathHelper.clamp(b, 0, 255);
        a = MathHelper.clamp(a, 0, 255);


        return new Color(r, g, b, a).getRGB();
    }

    /**
     * Устанавливает альфа-канал для цвета
     */
    public Color alpha(Color color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Применяет прозрачность (множитель от 0.0 до 1.0) к цвету (int)
     */
    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    /**
     * Применяет прозрачность к цвету (возвращает Color)
     */
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1.0F, Math.max(0.0F, opacity));
        int alpha = (int) (color.getAlpha() * opacity);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Плавный переход между двумя цветами
     */
    public Color fade(Color color1, Color color2, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        int r = (int) (color1.getRed() * (1 - alpha) + color2.getRed() * alpha);
        int g = (int) (color1.getGreen() * (1 - alpha) + color2.getGreen() * alpha);
        int b = (int) (color1.getBlue() * (1 - alpha) + color2.getBlue() * alpha);
        int a = (int) (color1.getAlpha() * (1 - alpha) + color2.getAlpha() * alpha);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return new Color(r, g, b, a);
    }

    /**
     * Пульсация цвета (изменение альфы по синусоиде)
     */
    public Color pulse(Color color, long speed) {
        speed = Math.max(1L, Math.min(30L, speed)); // избегаем деления на 0
        double t = (System.currentTimeMillis() - DrugHack.getInstance().getInitTime()) / 1000.0;
        double sin = Math.sin(Math.PI * 2 * (1.0 / speed) * t);
        double scale = (sin + 1.0) / 2.0;
        int newAlpha = (int) (color.getAlpha() * scale);
        newAlpha = Math.max(0, Math.min(255, newAlpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    /**
     * Уменьшает альфу цвета на заданный множитель
     */
    public Color offset(Color color, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int newAlpha = (int) (color.getAlpha() * alpha);
        newAlpha = Math.max(0, Math.min(255, newAlpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    /**
     * Глобальный градиентный цвет (фиолетовый от темного к светлому)
     * ВНИМАНИЕ: в оригинале был баг — цикл возвращал цвет сразу, не дойдя до нужного прогресса
     */
    public Color getGlobalColor(int alpha) {
        return new Color(118, 86, 211, alpha);
    }

    public Color getGlobalColor() {
        return getGlobalColor(255);
    }

    /**
     * Глобальный цвет с заданным прогрессом и альфой
     */
    public Color getGlobalColor1(float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        Color lightPurple = new Color(118, 86, 211);
        Color darkPurple = new Color(75, 82, 158);
        Color color = gradient(darkPurple, lightPurple, amount);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 255);
    }

    public Color getGlobalColor1() {
        return getGlobalColor1(0f);
    }

    /**
     * Линейный градиент между двумя цветами
     */
    public static Color gradient(Color color1, Color color2, float amount) {
        amount = Math.max(0.0f, Math.min(1.0f, amount));
        int r = (int) (color1.getRed() * (1 - amount) + color2.getRed() * amount);
        int g = (int) (color1.getGreen() * (1 - amount) + color2.getGreen() * amount);
        int b = (int) (color1.getBlue() * (1 - amount) + color2.getBlue() * amount);
        int a = (int) (color1.getAlpha() * (1 - amount) + color2.getAlpha() * amount);

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        return new Color(r, g, b, a);
    }

    /**
     * Многоступенчатый градиент через массив цветов
     */
    public Color multiGradient(Color[] colors, float progress) {
        if (colors == null || colors.length == 0) return Color.WHITE;
        if (colors.length == 1) return colors[0];

        progress = Math.max(0.0f, Math.min(1.0f, progress));
        float scaledProgress = progress * (colors.length - 1);
        int index = (int) Math.floor(scaledProgress);
        float localProgress = scaledProgress - index;

        if (index >= colors.length - 1) return colors[colors.length - 1];

        return fade(colors[index], colors[index + 1], localProgress);
    }

    /**
     * Создаёт цвет из HSV (Hue, Saturation, Value)
     */
    public Color fromHSV(float hue, float saturation, float value) {
        hue = ((hue % 360) + 360) % 360;
        saturation = Math.max(0.0f, Math.min(1.0f, saturation));
        value = Math.max(0.0f, Math.min(1.0f, value));

        float c = value * saturation;
        float x = c * (1 - Math.abs((hue / 60) % 2 - 1));
        float m = value - c;

        float r, g, b;
        if (hue < 60)      { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }

        int red = (int) ((r + m) * 255);
        int green = (int) ((g + m) * 255);
        int blue = (int) ((b + m) * 255);

        return new Color(
                Math.max(0, Math.min(255, red)),
                Math.max(0, Math.min(255, green)),
                Math.max(0, Math.min(255, blue))
        );
    }

    public static String minecraftGradient(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            Color color = gradient(start, end, progress);

            sb.append(getNearestColorCode(color)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getLetterGradientText(Color startColor, Color endColor, String text) {
        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            Color currentColor = gradient(startColor, endColor, progress);
            String colorCode = getNearestMinecraftColor(currentColor);
            result.append(colorCode).append(text.charAt(i));
        }

        return result.toString();
    }

    public static String getNearestMinecraftColor(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();


        if (r < 30 && g < 30 && b < 30) return "§0";
        if (r > 200 && g > 200 && b > 200) return "§f";
        if (r > g && r > b) {
            if (g > 150) return "§6";
            return "§c";
        }
        if (g > r && g > b) return "§a";
        if (b > r && b > g) return "§9";
        if (r > 200 && g > 200) return "§e";
        if (r > 200 && b > 200) return "§d";
        if (g > 200 && b > 200) return "§b";
        return "§f";
    }
    public static String getFullRGBGradient(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            Color color = gradient(start, end, progress);
            sb.append(getNearestMinecraftColor(color)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getUltraSmoothGradient(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int steps = text.length() * 2;

        for (int i = 0; i < text.length(); i++) {

            float progress1 = (float) (i * 2) / steps;
            Color color1 = gradient(start, end, progress1);


            float progress2 = (float) (i * 2 + 1) / steps;
            Color color2 = gradient(start, end, progress2);


            Color avgColor = new Color(
                    (color1.getRed() + color2.getRed()) / 2,
                    (color1.getGreen() + color2.getGreen()) / 2,
                    (color1.getBlue() + color2.getBlue()) / 2
            );

            sb.append(getNearestMinecraftColor(avgColor)).append(text.charAt(i));
        }

        return sb.toString();
    }

    public static String getHexColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    public static String getHexGradient(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / (length - 1);
            Color color = gradient(start, end, progress);
            sb.append("§x"); // Minecraft HEX-формат
            String hex = getHexColor(color).substring(1); // Убираем #
            for (char c : hex.toCharArray()) {
                sb.append("§").append(c);
            }
            sb.append(text.charAt(i));
        }

        return sb.toString();
    }
    public static String getSmoothGradientText(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int steps = text.length() * 2 - 1;

        for (int i = 0; i < text.length(); i++) {

            float progress1 = (float) (i*2) / steps;
            Color color1 = gradient(start, end, progress1);


            float progress2 = (float) (i*2 + 1) / steps;
            Color color2 = gradient(start, end, progress2);


            Color avgColor = new Color(
                    (color1.getRed() + color2.getRed())/2,
                    (color1.getGreen() + color2.getGreen())/2,
                    (color1.getBlue() + color2.getBlue())/2
            );

            sb.append(getNearestColorCode(avgColor))
                    .append(text.charAt(i));
        }

        return sb.toString();
    }
    public static String getGradientText(Color start, Color end, String text) {
        StringBuilder sb = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float progress = (float) i / Math.max(1, length - 1);
            Color color = gradient(start, end, progress);
            sb.append(getNearestColorCode(color)).append(text.charAt(i));
        }

        return sb.toString();
    }
    private static String getNearestColorCode(Color color) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        // Определяем ближайший Minecraft-цвет
        if (r < 30 && g < 30 && b < 30) return "§0";
        if (r > 200 && g < 50 && b < 50) return "§c";
        if (r < 50 && g > 200 && b < 50) return "§a";
        if (r < 50 && g < 50 && b > 200) return "§9";
        if (r > 200 && g > 200 && b < 50) return "§e";
        if (r > 200 && g < 50 && b > 200) return "§d";
        if (r < 50 && g > 200 && b > 200) return "§b";
        if (r > 200 && g > 200 && b > 200) return "§f";
        if (r > 150 && g > 100 && b < 50) return "§6";
        return "§f"; // По умолчанию белый
    }
    /**
     * Градиент с весами (взвешенная интерполяция)
     */
    public Color weightedGradient(Color[] colors, float[] weights, float progress) {
        if (colors == null || weights == null || colors.length != weights.length || colors.length == 0) {
            return Color.WHITE;
        }

        progress = Math.max(0.0f, Math.min(1.0f, progress));

        float totalWeight = 0;
        for (float w : weights) totalWeight += w;

        if (totalWeight <= 0) return colors[0];

        float accumulated = 0;
        for (int i = 0; i < weights.length; i++) {
            float normalizedWeight = weights[i] / totalWeight;
            if (progress <= accumulated + normalizedWeight) {
                float localProgress = (progress - accumulated) / normalizedWeight;
                localProgress = Math.max(0.0f, Math.min(1.0f, localProgress));
                if (i == weights.length - 1) return colors[i];
                return fade(colors[i], colors[i + 1], localProgress);
            }
            accumulated += normalizedWeight;
        }

        return colors[colors.length - 1];
    }
}