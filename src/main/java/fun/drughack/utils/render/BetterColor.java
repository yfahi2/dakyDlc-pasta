package fun.drughack.utils.render;

import fun.drughack.utils.math.MathUtils;
import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

@UtilityClass
public class BetterColor {
    // theme.get() >> 16 & 0xFF, theme.get() >> 8 & 0xFF, theme.get() & 0xFF,
    // , theme.get() >> 24 & 0xFF



    //тож не ебу откуда спастил



    // .gradient(ColorUtils.rgba(theme.get() >> 16 & 0xFF, theme.get() >> 8 & 0xFF, theme.get() & 0xFF, (int) (255 * effectsAlpha)), ColorUtils.darkenWithAlpha(ColorUtils.rgba(theme.get() >> 16 & 0xFF, theme.get() >> 8 & 0xFF, theme.get() & 0xFF, (int) (255 * effectsAlpha)), darkFactor.get()))
    public static float[] rgba(final int color) {
        return new float[] {
                (color >> 16 & 0xFF) / 255f,
                (color >> 8 & 0xFF) / 255f,
                (color & 0xFF) / 255f,
                (color >> 24 & 0xFF) / 255f
        };
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public Color interpolateColor(Color current, Color target, float speed, int alpha) {
        int red = (int) Math.max(0, Math.min(255, current.getRed() + (target.getRed() - current.getRed()) * speed));
        int green = (int) Math.max(0, Math.min(255, current.getGreen() + (target.getGreen() - current.getGreen()) * speed));
        int blue = (int) Math.max(0, Math.min(255, current.getBlue() + (target.getBlue() - current.getBlue()) * speed));
        return new Color(red, green, blue, alpha);
    }

    public Color interpolateColor(Color current, Color target, float speed) {
        int red = (int) Math.max(0, Math.min(255, current.getRed() + (target.getRed() - current.getRed()) * speed));
        int green = (int) Math.max(0, Math.min(255, current.getGreen() + (target.getGreen() - current.getGreen()) * speed));
        int blue = (int) Math.max(0, Math.min(255, current.getBlue() + (target.getBlue() - current.getBlue()) * speed));
        return new Color(red, green, blue);
    }

    public static int rgb(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }

    /**
     * Затемняет переданный цвет на заданный коэффициент.
     *
     * @param color Цвет для затемнения
     * @param factor Коэффициент затемнения (0.0f - без изменений, 1.0f - полностью черный)
     * @param alpha Прозрачность результата
     * @return Новый затемненный цвет
     */
    public static Color darken(Color color, float factor, int alpha) {
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        r = (int) (r * (1.0f - factor));
        g = (int) (g * (1.0f - factor));
        b = (int) (b * (1.0f - factor));

        r = (int) MathUtils.clamp(r, 0, 255);
        g = (int) MathUtils.clamp(g, 0, 255);
        b = (int) MathUtils.clamp(b, 0, 255);

        return new Color(r, g, b, alpha);
    }

    /**
     * Затемняет переданный цвет на заданный коэффициент, сохраняя исходную прозрачность.
     *
     * @param color Цвет для затемнения
     * @param factor Коэффициент затемнения (0.0f - без изменений, 1.0f - полностью черный)
     * @return Новый затемненный цвет
     */
    public static Color darken(Color color, float factor) {
        return darken(color, factor, color.getAlpha());
    }

    /**
     * Затемняет переданный цвет в формате int (RGB) на заданный коэффициент.
     *
     * @param color Цвет в формате int (RGB)
     * @param factor Коэффициент затемнения (0.0f - без изменений, 1.0f - полностью черный)
     * @return Новый затемненный цвет в формате int (RGB)
     */
    public static int darken(int color, float factor) {
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        float[] components = rgba(color);
        int r = (int) (components[0] * 255);
        int g = (int) (components[1] * 255);
        int b = (int) (components[2] * 255);

        r = (int) (r * (1.0f - factor));
        g = (int) (g * (1.0f - factor));
        b = (int) (b * (1.0f - factor));

        r = (int) MathUtils.clamp(r, 0, 255);
        g = (int) MathUtils.clamp(g, 0, 255);
        b = (int) MathUtils.clamp(b, 0, 255);

        return rgb(r, g, b);
    }

    public static int darkenWithAlpha(int color, float factor) {
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        // Извлекаем компоненты цвета (включая альфа)
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Затемняем RGB компоненты, сохраняя альфа-канал
        r = (int) (r * (1.0f - factor));
        g = (int) (g * (1.0f - factor));
        b = (int) (b * (1.0f - factor));

        // Обеспечиваем корректные границы
        r = Math.max(0, Math.min(r, 255));
        g = Math.max(0, Math.min(g, 255));
        b = Math.max(0, Math.min(b, 255));

        return rgba(r, g, b, a);
    }

    public static Color darkenWithAlpha(Color color, float factor) {
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        // Извлекаем компоненты цвета
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        // Затемняем RGB компоненты
        r = (int) (r * (1.0f - factor));
        g = (int) (g * (1.0f - factor));
        b = (int) (b * (1.0f - factor));

        // Обеспечиваем корректные границы
        r = Math.max(0, Math.min(r, 255));
        g = Math.max(0, Math.min(g, 255));
        b = Math.max(0, Math.min(b, 255));

        return new Color(r, g, b, a);
    }

    /**
     * Осветляет переданный цвет в формате java.awt.Color на заданный коэффициент.
     *
     * @param color  Цвет в формате java.awt.Color
     * @param factor Коэффициент осветления (0.0f - без изменений, 1.0f - полностью белый)
     * @return Новый осветленный цвет в формате java.awt.Color
     */
    public static Color lighten(Color color, float factor) {
        // Преобразуем Color в int (RGB) и используем существующий метод lighten
        int rgb = color.getRGB();
        int lightenedRgb = lighten(rgb, factor);
        // Возвращаем новый объект Color
        return new Color(lightenedRgb);
    }

    /**
     * Осветляет переданный цвет в формате java.awt.Color на заданный коэффициент с учетом альфа-канала.
     *
     * @param color  Цвет в формате java.awt.Color
     * @param factor Коэффициент осветления (0.0f - без изменений, 1.0f - полностью белый)
     * @return Новый осветленный цвет в формате java.awt.Color
     */
    public static Color lightenWithAlpha(Color color, float factor) {
        // Преобразуем Color в int (ARGB) и используем существующий метод lightenWithAlpha
        int argb = color.getRGB(); // getRGB() возвращает ARGB
        int lightenedArgb = lightenWithAlpha(argb, factor);
        // Возвращаем новый объект Color с сохранением альфа-канала
        return new Color(lightenedArgb, true);
    }

    /**
     * Осветляет переданный цвет в формате int (RGB) на заданный коэффициент.
     *
     * @param color  Цвет в формате int (RGB)
     * @param factor Коэффициент осветления (0.0f - без изменений, 1.0f - полностью белый)
     * @return Новый осветленный цвет в формате int (RGB)
     */
    public static int lighten(int color, float factor) {
        // Убедимся, что коэффициент находится в диапазоне от 0.0 до 1.0
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        // Получаем компоненты цвета в виде массива float [r, g, b, a] (0.0-1.0)
        float[] components = rgba(color);

        // Осветляем каждый компонент пр
        components[0] = components[0] * (1.0f - factor) + factor; // Red
        components[1] = components[1] * (1.0f - factor) + factor; // Green
        components[2] = components[2] * (1.0f - factor) + factor; // Blue

        // Преобразуем компоненты обратно в диапазон 0-255 и в int
        int r = (int) (components[0] * 255);
        int g = (int) (components[1] * 255);
        int b = (int) (components[2] * 255);

        // Убедимся, что компоненты находятся в допустимом диапазоне
        r = (int) MathUtils.clamp(r, 0, 255);
        g = (int) MathUtils.clamp(g, 0, 255);
        b = (int) MathUtils.clamp(b, 0, 255);

        return rgb(r, g, b);
    }

    /**
     * Осветляет переданный цвет в формате int (ARGB) на заданный коэффициент с учетом альфа-канала.
     *
     * @param color  Цвет в формате int (ARGB)
     * @param factor Коэффициент осветления (0.0f - без изменений, 1.0f - полностью белый)
     * @return Новый осветленный цвет в формате int (ARGB)
     */
    public static int lightenWithAlpha(int color, float factor) {
        factor = (float) MathUtils.clamp(factor, 0.0f, 1.0f);

        float[] components = rgba(color);

        components[0] = components[0] * (1.0f - factor) + factor; // Red
        components[1] = components[1] * (1.0f - factor) + factor; // Green
        components[2] = components[2] * (1.0f - factor) + factor; // Blue

        int r = (int) MathUtils.clamp((int) (components[0] * 255), 0, 255);
        int g = (int) MathUtils.clamp((int) (components[1] * 255), 0, 255);
        int b = (int) MathUtils.clamp((int) (components[2] * 255), 0, 255);
        int a = (int) (components[3] * 255);

        return rgba(r, g, b, a);
    }

    /**
     * Выполняет циклический градиентный переход между двумя цветами в формате int (RGBA) с плавным сглаживанием.
     *
     * @param color1   Первый цвет в формате int (RGBA)
     * @param color2   Второй цвет в формате int (RGBA)
     * @param speed    Скорость перехода (0.0f - 1.0f, где 0.01f - очень медленно, 1.0f - быстро)
     * @param tick     Текущий тик (например, System.currentTimeMillis() / 50)
     * @param duration Длительность полуцикла (от color1 до color2) в тиках
     * @return Текущий цвет в градиенте в формате int (RGBA)
     */
    public static int gradientColor(int color1, int color2, float speed, long tick, int duration) {
        // Полный цикл (туда и обратно)
        long fullCycle = duration * 2L;

        // Прогресс цикла (0.0 - 1.0)
        float rawProgress = (tick % fullCycle) / (float) fullCycle;

        // Используем синусоиду для сглаживания перехода
        float smoothedProgress = (float) (Math.sin(rawProgress * Math.PI * 2) + 1) / 2;

        // Применяем скорость и ограничиваем прогресс
        float progress = Math.max(0.0f, Math.min(1.0f, smoothedProgress * speed));

        // Извлекаем компоненты цветов
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        // Интерполяция компонентов
        int red = (int) (r1 + (r2 - r1) * progress);
        int green = (int) (g1 + (g2 - g1) * progress);
        int blue = (int) (b1 + (b2 - b1) * progress);
        int alpha = (int) (a1 + (a2 - a1) * progress);

        // Собираем итоговый цвет
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    /**
     * Проверяет, является ли цвет достаточно светлым (близким к белому),
     * чтобы на нем плохо читался белый/светлый текст.
     *
     * @param color Цвет для проверки
     * @return true, если цвет светлый (нужен темный текст), false иначе
     */
    public static boolean isLightColor(Color color) {
        // Конвертируем цвет в HSL для проверки lightness (яркости)
        float[] hsl = rgbToHsl(color.getRed(), color.getGreen(), color.getBlue());
        // Если lightness больше 0.7 (70%), считаем цвет светлым
        return hsl[2] > 0.7f;
    }

    /**
     * Проверяет, является ли цвет (в формате int RGB) достаточно светлым.
     *
     * @param rgb Цвет в формате int RGB
     * @return true, если цвет светлый (нужен темный текст), false иначе
     */
    public static boolean isLightColor(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        float[] hsl = rgbToHsl(r, g, b);
        return hsl[2] > 0.7f;
    }

    /**
     * Конвертирует RGB в HSL (Hue, Saturation, Lightness).
     *
     * @param r Красный компонент (0-255)
     * @param g Зеленый компонент (0-255)
     * @param b Синий компонент (0-255)
     * @return Массив float[] {h, s, l} где значения в диапазоне [0, 1]
     */
    private static float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h, s, l;
        l = (max + min) / 2f;

        if (delta == 0) {
            h = 0;
            s = 0;
        } else {
            s = delta / (1 - Math.abs(2 * l - 1));

            if (max == rf) {
                h = ((gf - bf) / delta) % 6;
            } else if (max == gf) {
                h = (bf - rf) / delta + 2;
            } else {
                h = (rf - gf) / delta + 4;
            }

            h /= 6;
            if (h < 0) h += 1;
        }

        return new float[] {h, s, l};
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = rgba(start);
        float[] endColor = rgba(end);

        return rgba((int) MathUtils.interpolate(startColor[0] * 255, endColor[0] * 255, value),
                (int) MathUtils.interpolate(startColor[1] * 255, endColor[1] * 255, value),
                (int) MathUtils.interpolate(startColor[2] * 255, endColor[2] * 255, value),
                (int) MathUtils.interpolate(startColor[3] * 255, endColor[3] * 255, value));
    }
}